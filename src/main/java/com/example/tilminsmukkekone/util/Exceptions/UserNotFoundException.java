package com.example.tilminsmukkekone.util.Exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("User with ID " + userId + " not found");
    }

    public UserNotFoundException(String username) {
        super("User with username '" + username + "' not found");
    }
}
