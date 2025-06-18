package com.boot.exception;

// RuntimeException을 상속받아 언체크 예외로 만듭니다.
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}