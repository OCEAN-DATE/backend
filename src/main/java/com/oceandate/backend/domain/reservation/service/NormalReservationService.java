package com.oceandate.backend.domain.reservation.service;

import com.oceandate.backend.domain.payment.entity.Coupon;
import com.oceandate.backend.domain.payment.repository.CouponRepository;
import com.oceandate.backend.domain.reservation.dto.CreateNormalReservationRequest;
import com.oceandate.backend.domain.reservation.dto.ReservationResponse;
import com.oceandate.backend.domain.reservation.entity.NormalReservation;
import com.oceandate.backend.domain.reservation.enums.NormalReservationStatus;
import com.oceandate.backend.domain.reservation.repository.NormalReservationRepository;
import com.oceandate.backend.domain.room.entity.Room;
import com.oceandate.backend.domain.room.repository.RoomRepository;
import com.oceandate.backend.domain.user.entity.UserEntity;
import com.oceandate.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NormalReservationService {

    private final NormalReservationRepository normalReservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    public ReservationResponse createReservation(Long userId, CreateNormalReservationRequest request) {

        // 1. 엔티티 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("객실을 찾을 수 없습니다."));

        // 2. 원래 가격 계산
        BigDecimal originalAmount = calculatePrice(room, request.getCheckInDate(), request.getCheckOutDate());

        // 3. 쿠폰 적용
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = originalAmount;
        Coupon appliedCoupon = null;

        if (request.getCouponId() != null) {
            appliedCoupon = couponRepository.findById(request.getCouponId())
                    .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

            discountAmount = calculateDiscount(originalAmount, appliedCoupon);
            finalAmount = originalAmount.subtract(discountAmount);
        }

        // 4. 예약 생성
        NormalReservation reservation = NormalReservation.builder()
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numberOfGuest(request.getNumberOfGuest())
                .status(NormalReservationStatus.PENDING)
                .build();

        // 상속 필드 설정
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setOriginalAmount(originalAmount);
        reservation.setDiscountAmount(discountAmount);
        reservation.setFinalAmount(finalAmount);
        reservation.setAppliedCoupon(appliedCoupon);
        reservation.setOrderId(generateOrderId());

        normalReservationRepository.save(reservation);

        // 5. 응답 생성
        return ReservationResponse.builder()
                .reservationId(reservation.getId())
                .orderId(reservation.getOrderId())
                .orderName(generateOrderName(reservation))
                .finalAmount(reservation.getFinalAmount())
                .build();
    }

    private BigDecimal calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return room.getBasePrice().multiply(BigDecimal.valueOf(nights));
    }

    private BigDecimal calculateDiscount(BigDecimal originalAmount, Coupon coupon) {
        return coupon.getDiscountAmount();
    }

    private String generateOrderId() {
        return "order_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateOrderName(NormalReservation reservation) {
        return String.format("%s 객실 예약 (%s ~ %s)",
                reservation.getRoom().getName(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );
    }
}