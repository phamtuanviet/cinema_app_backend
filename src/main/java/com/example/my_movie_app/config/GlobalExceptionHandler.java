package com.example.my_movie_app.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Hàm này sẽ "đứng rình", hễ có lỗi ResponseStatusException văng ra là nó tóm lại ngay
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {

        Map<String, String> errorBody = new HashMap<>();

        // Trích xuất chính xác câu thông báo tiếng Việt của bạn
        errorBody.put("message", ex.getReason());

        // Trả về đúng HTTP Status gốc (Ví dụ: 404 NOT_FOUND, 400 BAD_REQUEST)
        return ResponseEntity.status(ex.getStatusCode()).body(errorBody);
    }
}
