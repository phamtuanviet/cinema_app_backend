package com.example.my_movie_app.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Invalid or expired verification token");
    }
}