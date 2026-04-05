package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.response.UserPointsResponse;
import com.example.my_movie_app.entity.LoyaltyAccount;
import com.example.my_movie_app.repository.LoyaltyAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotUserService {

    private final LoyaltyAccountRepository loyaltyRepository;

    @Transactional(readOnly = true)
    public UserPointsResponse getMyPoints(UUID userId) {
        LoyaltyAccount account = loyaltyRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin thành viên."));

        int points = account.getAvailablePoints();

        String formattedValue = String.format("%,d VNĐ", points);

        return UserPointsResponse.builder()
                .availablePoints(points)
                .monetaryValueFormatted(formattedValue)
                .accumulationPolicy("Bạn sẽ được tích lũy thêm 2% giá trị hóa đơn vào điểm thưởng cho mỗi lần mua vé.")
                .note("Số điểm này có thể dùng để giảm giá trực tiếp cho các lần đặt vé tiếp theo!")
                .build();
    }

}