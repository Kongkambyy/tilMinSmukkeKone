package com.example.tilminsmukkekone.util.Exceptions;

public class AnniversaryException extends RuntimeException {
    public AnniversaryException(String message) {
        super(message);
    }

    public static AnniversaryException dateNotSetException() {
        return new AnniversaryException("Anniversary date has not been set");
    }
}
