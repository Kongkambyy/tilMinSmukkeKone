package com.example.tilminsmukkekone.infrastructure.util;

import java.sql.SQLException;

public class DatabaseException extends RuntimeException {

    private final String operation;
    private final String entity;

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.operation = "unknown";
        this.entity = "unknown";
    }

    public DatabaseException(String operation, String entity, String message, Throwable cause) {
        super(message, cause);
        this.operation = operation;
        this.entity = entity;
    }

    public static DatabaseException createError(String operation, String entity, SQLException e) {
        String message = String.format("Database error during %s operation on %s entity: %s",
                operation, entity, e.getMessage());
        return new DatabaseException(message, operation, entity, e);
    }

    public static DatabaseException readError(String entity, Object identifier, SQLException e) {
        String message = String.format("Failed to read %s with identifier %s: %s",
                entity, identifier, e.getMessage());
        return new DatabaseException(message, "read", entity, e);
    }

    public static DatabaseException writeError(String entity, SQLException e) {
        String message = String.format("Failed to save %s to database: %s",
                entity, e.getMessage());
        return new DatabaseException(message, "write", entity, e);
    }

    public static DatabaseException updateError(String entity, Object identifier, SQLException e) {
        String message = String.format("Failed to update %s with identifier %s: %s",
                entity, identifier, e.getMessage());
        return new DatabaseException(message, "update", entity, e);
    }

    public static DatabaseException deleteError(String entity, Object identifier, SQLException e) {
        String message = String.format("Failed to delete %s with identifier %s: %s",
                entity, identifier, e.getMessage());
        return new DatabaseException(message, "delete", entity, e);
    }

    public String getOperation() {
        return operation;
    }

    public String getEntity() {
        return entity;
    }
}
