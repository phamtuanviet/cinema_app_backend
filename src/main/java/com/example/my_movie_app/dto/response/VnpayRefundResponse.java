package com.example.my_movie_app.dto.response;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class VnpayRefundResponse {

    @JsonProperty("vnp_ResponseId")
    private String vnp_ResponseId;

    @JsonProperty("vnp_Command")
    private String vnp_Command;

    @JsonProperty("vnp_ResponseCode")
    private String vnp_ResponseCode;

    @JsonProperty("vnp_Message")
    private String vnp_Message;

    @JsonProperty("vnp_TmnCode")
    private String vnp_TmnCode;

    @JsonProperty("vnp_TxnRef")
    private String vnp_TxnRef;

    @JsonProperty("vnp_Amount")
    private String vnp_Amount;

    @JsonProperty("vnp_BankCode")
    private String vnp_BankCode;

    @JsonProperty("vnp_PayDate")
    private String vnp_PayDate;

    @JsonProperty("vnp_TransactionNo")
    private String vnp_TransactionNo;

    @JsonProperty("vnp_TransactionType")
    private String vnp_TransactionType;

    @JsonProperty("vnp_TransactionStatus")
    private String vnp_TransactionStatus;

    @JsonProperty("vnp_OrderInfo")
    private String vnp_OrderInfo;

    @JsonProperty("vnp_SecureHash")
    private String vnp_SecureHash;
}