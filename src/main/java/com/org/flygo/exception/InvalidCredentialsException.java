package com.org.flygo.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String invalidEmailOrPassword) {
        super("Invalid email or password: " + invalidEmailOrPassword);

    }
}
