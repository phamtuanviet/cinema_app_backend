package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.request.VnpayRefundRequest;
import com.example.my_movie_app.dto.response.VnpayRefundResponse;
import com.example.my_movie_app.entity.*;
import com.example.my_movie_app.enums.BookingStatus;
import com.example.my_movie_app.enums.LoyaltyTransactionType;
import com.example.my_movie_app.enums.PaymentStatus;
import com.example.my_movie_app.enums.UsageStatus;
import com.example.my_movie_app.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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

    @Value("${vnpay.api.url}")
    private String vnpApiUrl;

    private final BookingRepository bookingRepository;

    private final PaymentRepository paymentRepository;

    private final VoucherUsageRepository voucherUsageRepository;

    private final LoyaltyAccountRepository loyaltyAccountRepository;

    private final RestTemplate restTemplate;

    private final UserVoucherRepository userVoucherRepository;

    private final LoyaltyTransactionRepository loyaltyTransactionRepository;


    @Transactional
    public String createPaymentUrl(UUID bookingId, HttpServletRequest request) {

        Booking booking = bookingRepository.findByIdWithSession(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));

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
        String amount = booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toPlainString();

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

                hashData.append(name).append("=").append(URLEncoder.encode(value, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(name, StandardCharsets.US_ASCII)).append("=").append(URLEncoder.encode(value, StandardCharsets.US_ASCII));

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


    @Transactional
    public VnpayRefundResponse refundTransaction(UUID bookingId, String ipAddr, String createBy) {

        // 1. Lấy thông tin Booking và Giao dịch thanh toán
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));

        // Giả sử bạn có PaymentEntity lưu thông tin giao dịch lúc thanh toán thành công
        // Cần thiết để lấy vnp_TransactionDate của lúc Pay
        Payment payment = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.SUCCESS).orElseThrow(() -> new RuntimeException("Successful payment not found for booking"));

        // 2. Chuẩn bị dữ liệu định dạng ngày tháng

        // 3. Chuẩn bị các tham số bắt buộc
        String vnp_RequestId = UUID.randomUUID().toString().replace("-", "");
        String vnp_Version = "2.1.0";
        String vnp_Command = "refund";
        String vnp_TransactionType = "02";
        String vnp_TxnRef = booking.getTicketCode();

        String vnp_Amount = booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toPlainString();

        String vnp_OrderInfo = "Hoan tien GD OrderId:" + vnp_TxnRef;

        // 🔥 DÙNG CHÍNH MÃ GIAO DỊCH VÀ NGÀY GIAO DỊCH TỪ LÚC PAY
        String vnp_TransactionNo = payment.getGatewayTransactionId() != null ? payment.getGatewayTransactionId() : "";
        String vnp_TransactionDate = payment.getVnpPayDate();

        if (vnp_TransactionDate == null) {
            throw new RuntimeException("Missing vnpPayDate in Database. Cannot refund old tickets.");
        }

        // Thời gian tạo request (hiện tại)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(new Date());

        // 3. TẠO HASH Y HỆT MẪU VNPAY (An toàn tuyệt đối)
        String hash_Data = String.join("|", vnp_RequestId, vnp_Version, vnp_Command, tmnCode, vnp_TransactionType, vnp_TxnRef, vnp_Amount, vnp_TransactionNo, vnp_TransactionDate, createBy, vnp_CreateDate, ipAddr, vnp_OrderInfo);

        String vnp_SecureHash = hmacSHA512(hashSecret, hash_Data);

        // 4. Tạo Request Body (Đã có @JsonProperty trong class này rồi nhé)
        VnpayRefundRequest requestData = VnpayRefundRequest.builder().vnp_RequestId(vnp_RequestId).vnp_Version(vnp_Version).vnp_Command(vnp_Command).vnp_TmnCode(tmnCode).vnp_TransactionType(vnp_TransactionType).vnp_TxnRef(vnp_TxnRef).vnp_Amount(vnp_Amount).vnp_TransactionNo(vnp_TransactionNo).vnp_TransactionDate(vnp_TransactionDate) // <-- Nạp đúng String cũ vào đây
                .vnp_CreateBy(createBy).vnp_CreateDate(vnp_CreateDate).vnp_IpAddr(ipAddr).vnp_OrderInfo(vnp_OrderInfo).vnp_SecureHash(vnp_SecureHash).build();

        // 6. Gửi Request lên VNPAY
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<VnpayRefundRequest> entity = new HttpEntity<>(requestData, headers);


        try {
            VnpayRefundResponse response = restTemplate.postForObject(vnpApiUrl, entity, VnpayRefundResponse.class);
            System.out.println("REFUND GAMING");
            if (response != null) {
                System.out.println("REFUND GAMING DOMIXI");
                System.out.println(response);
                if ("00".equals(response.getVnp_ResponseCode())) {

                    booking.setStatus(BookingStatus.REFUNDED);
                    bookingRepository.save(booking);

                    // ==========================================
                    // 2. HOÀN TRẢ VOUCHER (NẾU CÓ DÙNG)
                    // ==========================================
                    if (booking.getVoucherDiscount() != null && booking.getVoucherDiscount().compareTo(BigDecimal.ZERO) > 0) {

                        // Tìm giao dịch sử dụng voucher của booking này
                        voucherUsageRepository.findByBooking_Id(booking.getId()).ifPresent(usage -> {

                            usage.setStatus(UsageStatus.REFUNDED);
                            voucherUsageRepository.save(usage);

                            // 2.2 Kích hoạt lại UserVoucher để khách hàng có thể dùng lần sau
                            UserVoucher userVoucher = usage.getUserVoucher();
                            if (userVoucher != null) {
                                userVoucher.setIsUsed(false);
                                userVoucher.setUsedAt(null);
                                userVoucherRepository.save(userVoucher);
                            }
                        });
                    }

                    int earnedPoints = booking.getTotalAmount().divide(BigDecimal.valueOf(50), RoundingMode.DOWN).intValue();

                    int pointDiscount = (booking.getPointDiscount() != null) ? booking.getPointDiscount().intValue() : 0;

                    int pointsToRefund = pointDiscount - earnedPoints;

                    if (pointsToRefund != 0) {
                        loyaltyAccountRepository.findByUser_Id(booking.getUser().getId()).ifPresent(account -> {

                            // 3.1 Cộng lại điểm vào tài khoản
                            account.setAvailablePoints(account.getAvailablePoints() + pointsToRefund);
                            loyaltyAccountRepository.save(account);

                            // 3.2 Ghi lại lịch sử giao dịch trả điểm
                            LoyaltyTransaction refundTransaction = LoyaltyTransaction.builder().account(account).points(pointsToRefund) // Giá trị dương
                                    .type(LoyaltyTransactionType.REFUND) // Bạn nên thêm type REFUND vào Enum
                                    .description("Refund points for canceled booking " + booking.getTicketCode()).booking(booking).build();

                            loyaltyTransactionRepository.save(refundTransaction);
                        });
                    }

                    payment.setStatus(PaymentStatus.REFUNDED);
                    paymentRepository.save(payment);


                    return response;

                } else {
                    throw new RuntimeException("Refund failed with code: " + response.getVnp_ResponseCode() + " - " + response.getVnp_Message());
                }
            } else {
                throw new RuntimeException("No response from VNPAY");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error calling VNPAY Refund API", e);
        }
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

                hashData.append(name).append("=").append(URLEncoder.encode(value, StandardCharsets.US_ASCII));

                first = false;
            }
        }

        String signValue = hmacSHA512(hashSecret, hashData.toString());

        return signValue.equals(vnp_SecureHash);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

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