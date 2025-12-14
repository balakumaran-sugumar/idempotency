package com.balakumaran.idempotent.idempotencycheck.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    private String paymentId;

    private String accountInformation;

    private String paymentAmt;

}
