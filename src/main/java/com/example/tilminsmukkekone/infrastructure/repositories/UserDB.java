package com.example.tilminsmukkekone.infrastructure.repositories;

import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.infrastructure.util.DatabaseException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class UserDB {

    private final DatabaseOperations dbOps;

    public UserDB(DataSource dataSource) {
        this.dbOps = new DatabaseOperations(dataSource);
    }

    private User mapResultSetToUser(ResultSet rs) {
        try {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));

            java.sql.Date anniversaryDate = rs.getDate("anniversary");
            if (anniversaryDate != null) {
                user.setAnniversary(anniversaryDate.toLocalDate());
            }

            user.setPhotoUrl(rs.getString("photo_url"));
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Error mapping database row to User object",
                    "mapping", "user", e);
        }
    }

    public Optional<User> findById(Long id) {
        try {
            String sql = "SELECT * FROM users WHERE id = ?";
            User user = dbOps.executeQueryForSingleResult(sql, new Object[]{id}, this::mapResultSetToUser);
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            throw DatabaseException.readError("user", id, e);
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            String sql = "SELECT * FROM users WHERE username = ?";
            User user = dbOps.executeQueryForSingleResult(sql, new Object[]{username}, this::mapResultSetToUser);
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            throw DatabaseException.readError("user", username, e);
        }
    }

    public List<User> findAll() {
        try {
            String sql = "SELECT * FROM users";
            return dbOps.executeQuery(sql, null, this::mapResultSetToUser);
        } catch (SQLException e) {
            throw DatabaseException.createError("read_all", "user", e);
        }
    }

    public Long save(User user) {
        try {
            String sql = "INSERT INTO users (username, password, name, anniversary, photo_url) " +
                    "VALUES (?, ?, ?, ?, ?)";

            Object[] params = new Object[]{
                    user.getUsername(),
                    user.getPassword(),
                    user.getName(),
                    user.getAnniversary() != null ? java.sql.Date.valueOf(user.getAnniversary()) : null,
                    user.getPhotoUrl()
            };

            return dbOps.executeInsertAndGetId(sql, params);
        } catch (SQLException e) {
            throw DatabaseException.writeError("user", e);
        }
    }

    public boolean update(User user) {
        try {
            String sql = "UPDATE users SET username = ?, password = ?, name = ?, " +
                    "anniversary = ?, photo_url = ? WHERE id = ?";

            Object[] params = new Object[]{
                    user.getUsername(),
                    user.getPassword(),
                    user.getName(),
                    user.getAnniversary() != null ? java.sql.Date.valueOf(user.getAnniversary()) : null,
                    user.getPhotoUrl(),
                    user.getId()
            };

            int rowsAffected = dbOps.executeUpdate(sql, params);
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.updateError("user", user.getId(), e);
        }
    }

    public boolean deleteById(Long id) {
        try {
            String sql = "DELETE FROM users WHERE id = ?";
            int rowsAffected = dbOps.executeUpdate(sql, new Object[]{id});
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.deleteError("user", id, e);
        }
    }

    public boolean existsByUsername(String username) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            Integer count = dbOps.executeQueryForSingleResult(sql, new Object[]{username}, rs -> {
                try {
                    return rs.getInt(1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return count != null && count > 0;
        } catch (SQLException e) {
            throw DatabaseException.createError("exists_check", "user", e);
        }
    }

    public int countUsers() {
        try {
            String sql = "SELECT COUNT(*) FROM users";
            Integer count = dbOps.executeQueryForSingleResult(sql, null, rs -> {
                try {
                    return rs.getInt(1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return count != null ? count : 0;
        } catch (SQLException e) {
            throw DatabaseException.createError("count", "user", e);
        }
    }
}