package com.oceandate.backend.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oceandate.backend.domain.payment.enums.PaymentMethod;
import com.oceandate.backend.domain.payment.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class TossPaymentResponse {

    private String version;
    private String paymentKey;
    private String type;
    private String orderId;
    private String orderName;
    private String mId;
    private String currency;

    @JsonProperty("method")
    private PaymentMethod method;

    private Integer totalAmount;
    private Integer balanceAmount;

    @JsonProperty("status")
    private PaymentStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    private Boolean useEscrow;
    private String lastTransactionKey;

    private Integer suppliedAmount;
    private Integer vat;

    private Boolean cultureExpense;
    private Integer taxFreeAmount;
    private Integer taxExemptionAmount;

    private List<Cancel> cancels;
    private Boolean isPartialCancelable;

    private Card card;
    private VirtualAccount virtualAccount;
    private MobilePhone mobilePhone;
    private GiftCertificate giftCertificate;
    private Transfer transfer;
    private EasyPay easyPay;

    private Receipt receipt;
    private Checkout checkout;
    private CashReceipt cashReceipt;
    private List<CashReceipts> cashReceipts;
    private Discount discount;
    private Failure failure;

    private String country;

    @Getter
    @NoArgsConstructor
    public static class Cancel {
        private Integer cancelAmount;
        private String cancelReason;
        private Integer taxFreeAmount;
        private Integer taxExemptionAmount;
        private Integer refundableAmount;
        private Integer cardDiscountAmount;
        private Integer transferDiscountAmount;
        private Integer easyPayDiscountAmount;
        private LocalDateTime canceledAt;
        private String transactionKey;
        private String receiptKey;
        private String cancelStatus;
        private String cancelRequestId;
    }

    @Getter
    @NoArgsConstructor
    public static class Card {
        private Integer amount;
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private Integer installmentPlanMonths;
        private String approveNo;
        private Boolean useCardPoint;
        private String cardType;
        private String ownerType;
        private String acquireStatus;
        private Boolean isInterestFree;
        private String interestPayer;
    }

    @Getter
    @NoArgsConstructor
    public static class VirtualAccount {
        private String accountType;
        private String accountNumber;
        private String bankCode;
        private String customerName;
        private String depositorName;
        private LocalDateTime dueDate;
        private String refundStatus;
        private Boolean expired;
        private String settlementStatus;
        private RefundReceiveAccount refundReceiveAccount;
        private String secret;
    }

    @Getter
    @NoArgsConstructor
    public static class RefundReceiveAccount {
        private String bankCode;
        private String accountNumber;
        private String holderName;
    }

    @Getter
    @NoArgsConstructor
    public static class MobilePhone {
        private String customerMobilePhone;
        private String settlementStatus;
        private String receiptUrl;
    }

    @Getter
    @NoArgsConstructor
    public static class GiftCertificate {
        private String approveNo;
        private String settlementStatus;
    }

    @Getter
    @NoArgsConstructor
    public static class Transfer {
        private String bankCode;
        private String settlementStatus;
    }

    @Getter
    @NoArgsConstructor
    public static class EasyPay {
        private String provider;
        private Integer amount;
        private Integer discountAmount;
    }

    @Getter
    @NoArgsConstructor
    public static class Receipt {
        private String url;
    }

    @Getter
    @NoArgsConstructor
    public static class Checkout {
        private String url;
    }

    @Getter
    @NoArgsConstructor
    public static class CashReceipt {
        private String type;
        private String receiptKey;
        private String issueNumber;
        private String receiptUrl;
        private Integer amount;
        private Integer taxFreeAmount;
    }

    @Getter
    @NoArgsConstructor
    public static class CashReceipts {
        private String receiptKey;
        private String orderId;
        private String orderName;
        private String type;
        private String issueNumber;
        private String receiptUrl;
        private String businessNumber;
        private String transactionType;
        private Integer amount;
        private Integer taxFreeAmount;
        private String issueStatus;
        private Failure failure;
        private String customerIdentityNumber;
        private LocalDateTime requestedAt;
    }

    @Getter
    @NoArgsConstructor
    public static class Discount {
        private Integer amount;
    }

    @Getter
    @NoArgsConstructor
    public static class Failure {
        private String code;
        private String message;
    }
}