package com.example.my_movie_app.service;

import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.SeatHoldSession;
import com.example.my_movie_app.repository.BookingRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnpayService {

    @Value("${spring.vnpay.vnp_TmnCode}")
    private String tmnCode;

    @Value("${spring.vnpay.vnp_HashSecret}")
    private String hashSecret;

    @Value("${spring.vnpay.vnp_Url}")
    private String payUrl;

    @Value("${spring.vnpay.vnp_ReturnUrl}")
    private String returnUrl;

    private final BookingRepository bookingRepository;

    public String createPaymentUrl(UUID bookingId, HttpServletRequest request) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        SeatHoldSession session = booking.getSession();

        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Session expired");
        }

        // ===== TIME =====
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String createDate = formatter.format(cld.getTime());

        Date expireDate = Date.from(session.getExpiresAt());
        String expire = formatter.format(expireDate);

        // ===== AMOUNT (NO DECIMAL) =====
        String amount = booking.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();

        // ===== PARAMS =====
        Map<String, String> params = new HashMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", amount);
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", booking.getTicketCode());
        params.put("vnp_OrderInfo", "Thanh_toan_booking_" + bookingId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);

        // ⚠️ FIX IP
        params.put("vnp_IpAddr", "127.0.0.1");

        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expire);

        // ===== SORT PARAMS =====
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        boolean first = true;

        for (String name : fieldNames) {
            String value = params.get(name);

            if (value != null && !value.isEmpty()) {

                if (!first) {
                    hashData.append("&");
                    query.append("&");
                }

                hashData.append(name)
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(name, StandardCharsets.US_ASCII))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));

                first = false;
            }
        }

        // ===== HASH =====
        String secureHash = hmacSHA512(hashSecret, hashData.toString());

        String paymentUrl = payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;

        // DEBUG
        System.out.println("HASH DATA: " + hashData);
        System.out.println("PAYMENT URL: " + paymentUrl);

        return paymentUrl;
    }

    public boolean verify(Map<String, String> params) {

        String vnp_SecureHash = params.remove("vnp_SecureHash");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        boolean first = true;

        for (String name : fieldNames) {
            String value = params.get(name);

            if (value != null && !value.isEmpty()) {

                if (!first) {
                    hashData.append("&");
                }

                hashData.append(name)
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));

                first = false;
            }
        }

        String signValue = hmacSHA512(hashSecret, hashData.toString());

        return signValue.equals(vnp_SecureHash);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );

            mac.init(secretKey);

            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }

            return hash.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error while hashing", e);
        }
    }
}