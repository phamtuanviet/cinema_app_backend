package com.example.my_movie_app.dto.request;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;


@Data
@Builder
public class VnpayRefundRequest {

    @JsonProperty("vnp_RequestId")
    private String vnp_RequestId;

    @JsonProperty("vnp_Version")
    private String vnp_Version;

    @JsonProperty("vnp_Command")
    private String vnp_Command;

    @JsonProperty("vnp_TmnCode")
    private String vnp_TmnCode;

    @JsonProperty("vnp_TransactionType")
    private String vnp_TransactionType;

    @JsonProperty("vnp_TxnRef")
    private String vnp_TxnRef;

    @JsonProperty("vnp_Amount")
    private String vnp_Amount;

    @JsonProperty("vnp_TransactionNo")
    private String vnp_TransactionNo;

    @JsonProperty("vnp_TransactionDate")
    private String vnp_TransactionDate;

    @JsonProperty("vnp_CreateBy")
    private String vnp_CreateBy;

    @JsonProperty("vnp_CreateDate")
    private String vnp_CreateDate;

    @JsonProperty("vnp_IpAddr")
    private String vnp_IpAddr;

    @JsonProperty("vnp_OrderInfo")
    private String vnp_OrderInfo;

    @JsonProperty("vnp_SecureHash")
    private String vnp_SecureHash;
}