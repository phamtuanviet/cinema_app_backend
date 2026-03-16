package com.example.my_movie_app.service;

import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnpayService {

    @Value("${spring.vnpay.vnp_TmnCode}")
    private String tmnCode;

    @Value("${spring.vnpay.vnp_HashSecret}")
    private String secretKey;

    @Value("${spring.vnpay.vnp_Url}")
    private String vnpUrl;

    @Value("${spring.vnpay.vnp_ReturnUrl}")
    private String returnUrl;

    public String createPaymentUrl(Booking booking, Payment payment){

        Map<String,String> params = new HashMap<>();

        params.put("vnp_Version","2.1.0");
        params.put("vnp_Command","pay");
        params.put("vnp_TmnCode",tmnCode);

        params.put("vnp_Amount",
                booking.getTotalAmount()
                        .multiply(new java.math.BigDecimal(100))
                        .toString());

        params.put("vnp_CurrCode","VND");

        params.put("vnp_TxnRef", payment.getId().toString());

        params.put("vnp_OrderInfo",
                "Payment booking " + booking.getTicketCode());

        params.put("vnp_OrderType","other");

        params.put("vnp_ReturnUrl",returnUrl);

        params.put("vnp_IpAddr","127.0.0.1");

        params.put("vnp_Locale","vn");

        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyyMMddHHmmss");

        params.put("vnp_CreateDate",
                formatter.format(new Date()));

        List<String> fieldNames =
                new ArrayList<>(params.keySet());

        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for(String field : fieldNames){

            String value = params.get(field);

            if(hashData.length() > 0){
                hashData.append("&");
                query.append("&");
            }

            hashData.append(field).append("=").append(value);

            query.append(field)
                    .append("=")
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }

        String secureHash = hmacSHA512(secretKey, hashData.toString());

        query.append("&vnp_SecureHash=").append(secureHash);

        return vnpUrl + "?" + query;
    }

    private String hmacSHA512(String key, String data){

        try {

            Mac mac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(),"HmacSHA512");

            mac.init(secretKey);

            byte[] hash = mac.doFinal(data.getBytes());

            StringBuilder hex = new StringBuilder();

            for(byte b : hash){

                String s = Integer.toHexString(0xff & b);

                if(s.length()==1) hex.append('0');

                hex.append(s);
            }

            return hex.toString();

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
