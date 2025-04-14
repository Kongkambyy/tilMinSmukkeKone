package com.example.tilminsmukkekone.infrastructure.repositories;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class DatabaseOperations {

    private final DataSource dataSource;

    public DatabaseOperations(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> executeQuery(String sql, Object[] params, Function<ResultSet, T> mapper) throws SQLException {
        List<T> results = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapper.apply(resultSet));
                }
            }
        }

        return results;
    }

    public <T> T executeQueryForSingleResult(String sql, Object[] params, Function<ResultSet, T> mapper) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapper.apply(resultSet);
                }
                return null;
            }
        }
    }

    public int executeUpdate(String sql, Object[] params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }

            return statement.executeUpdate();
        }
    }

    public Long executeInsertAndGetId(String sql, Object[] params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating record failed, no ID obtained.");
                }
            }
        }
    }

    public int[] executeBatch(String sql, List<Object[]> batchParams) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Object[] params : batchParams) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                statement.addBatch();
            }

            return statement.executeBatch();
        }
    }
}