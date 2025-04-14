package com.example.tilminsmukkekone.util.Exceptions;

public class MemoryNotFoundException extends RuntimeException {
    public MemoryNotFoundException(Long memoryId) {
        super("Memory with ID " + memoryId + " not found");
    }
}