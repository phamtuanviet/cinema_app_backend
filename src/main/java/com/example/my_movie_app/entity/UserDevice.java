package com.example.my_movie_app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_devices",
        indexes = {
                // Tối ưu Index: Đánh index cột fcm_token để tìm kiếm và xóa cực nhanh
                @Index(name = "idx_fcm_token", columnList = "fcm_token", unique = true),
                @Index(name = "idx_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) // LUÔN dùng LAZY cho ManyToOne để tránh N+1 Query
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    @Column(name = "device_type", length = 50)
    private String deviceType; // Gợi ý: Lưu chữ "ANDROID", "IOS", hoặc "WEB"

    @Column(name = "is_active", nullable = false)
    private boolean active = true; // Dùng để vô hiệu hóa token thay vì xóa cứng (nếu cần)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Dùng để biết thiết bị này còn hoạt động gần đây không
}