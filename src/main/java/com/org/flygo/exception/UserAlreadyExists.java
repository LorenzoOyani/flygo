package com.org.flygo.exception;

public class UserAlreadyExists extends RuntimeException {
    public UserAlreadyExists(String userAlreadyExits) {
        super("user already exits: " + userAlreadyExits);
    }
}
