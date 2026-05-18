package com.example.my_movie_app.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j // Thay thế System.out.println bằng chuẩn Log chuyên nghiệp
@Service
public class FcmService {

    @Async
    public void sendPushNotification(String targetToken, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);
        } catch (Exception e) {
            System.err.println("Error sending FCM message: " + e.getMessage());
        }
    }

    @Async
    public void sendGlobalNotificationByTopic(String topic, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setTopic(topic) // Khác biệt duy nhất: Dùng setTopic thay vì setToken
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            // Chỉ gắn data nếu có dữ liệu
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance().send(message);

            log.info("🚀 Gửi FCM Broadcast tới topic '{}' thành công: {}", topic, response);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi FCM Broadcast tới topic '{}': {}", topic, e.getMessage(), e);
        }
    }
}