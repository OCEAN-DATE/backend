package com.oceandate.backend.domain.payment.controller;

import com.oceandate.backend.domain.payment.client.TossPaymentClient;
import com.oceandate.backend.domain.payment.dto.ConfirmPaymentRequest;
import com.oceandate.backend.domain.payment.dto.TossPaymentResponse;
import com.oceandate.backend.domain.payment.entity.Payment;
import com.oceandate.backend.domain.reservation.entity.NormalReservation;
import com.oceandate.backend.domain.reservation.entity.DateReservation;
import com.oceandate.backend.domain.reservation.repository.NormalReservationRepository;
import com.oceandate.backend.domain.reservation.repository.DateReservationRepository;
import com.oceandate.backend.domain.reservation.enums.NormalReservationStatus;
import com.oceandate.backend.domain.reservation.enums.DateReservationStatus;
import com.oceandate.backend.domain.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;
    private final NormalReservationRepository normalReservationRepository;
    private final DateReservationRepository dateReservationRepository;

    @PostMapping("/confirm")
    @Transactional
    public ResponseEntity<?> confirmPayment(@RequestBody ConfirmPaymentRequest request) {

        try {
            // 1. DB에서 예약 조회
            Reservation reservation;

            if ("NORMAL".equals(request.getReservationType())) {
                reservation = normalReservationRepository.findByOrderId(request.getOrderId())
                        .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
            } else {
                reservation = dateReservationRepository.findByOrderId(request.getOrderId())
                        .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
            }

            // 2. 금액 검증
            if (!reservation.getFinalAmount().equals(request.getAmount())) {
                return ResponseEntity.badRequest().body(
                        "결제 금액 불일치: 예상=" + reservation.getFinalAmount() +
                                ", 실제=" + request.getAmount()
                );
            }

            // 3. Toss API 호출
            HttpResponse<String> response = tossPaymentClient.requestConfirm(
                    new ConfirmPaymentRequest(request.getPaymentKey(),
                            request.getOrderId(),
                            request.getAmount(),
                            request.getReservationType())
            );

            if (response.statusCode() == 200) {
                TossPaymentResponse tossResponse =
                        objectMapper.readValue(response.body(), TossPaymentResponse.class);

                // 4. Payment 엔티티 생성
                Payment payment = Payment.builder()
                        .paymentKey(tossResponse.getPaymentKey())
                        .orderId(request.getOrderId())
                        .amount(request.getAmount())
                        .method(tossResponse.getMethod())
                        .status(tossResponse.getStatus())
                        .build();

                // 5. 예약 상태 업데이트
                if (reservation instanceof NormalReservation normalReservation) {
                    normalReservation.setStatus(NormalReservationStatus.CONFIRMED);
                    normalReservation.setPayment(payment);
                    payment.setNormalReservation(normalReservation);
                } else if (reservation instanceof DateReservation dateReservation) {
                    dateReservation.setStatus(DateReservationStatus.PAYMENT_COMPLETED);
                    dateReservation.setPayment(payment);
                    payment.setDateReservation(dateReservation);
                }

                return ResponseEntity.ok(tossResponse);

            } else {
                TossPaymentResponse.Failure failureResponse =
                        objectMapper.readValue(response.body(), TossPaymentResponse.Failure.class);

                return ResponseEntity.status(response.statusCode()).body(failureResponse);
            }

        } catch(Exception e) {
            return ResponseEntity.internalServerError()
                    .body("결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}