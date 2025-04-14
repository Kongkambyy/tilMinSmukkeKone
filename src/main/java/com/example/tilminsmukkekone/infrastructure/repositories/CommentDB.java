package com.example.tilminsmukkekone.infrastructure.repositories;

import com.example.tilminsmukkekone.domain.classes.Comment;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.infrastructure.util.DatabaseException;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class CommentDB {

    private final DatabaseOperations dbOps;
    private final UserDB userDB;

    public CommentDB(DatabaseOperations dbOps, UserDB userDB) {
        this.dbOps = dbOps;
        this.userDB = userDB;
    }

    private Comment mapResultSetToComment(ResultSet rs) {
        try {
            Comment comment = new Comment();
            comment.setId(rs.getLong("id"));
            comment.setContent(rs.getString("content"));
            comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            Long authorId = rs.getLong("author_id");
            Optional<User> author = userDB.findById(authorId);
            author.ifPresent(comment::setAuthor);

            return comment;
        } catch (SQLException e) {
            throw new DatabaseException("Error mapping database row to Comment object",
                    "mapping", "comment", e);
        }
    }

    public Optional<Comment> findById(Long id) {
        try {
            String sql = "SELECT * FROM comments WHERE id = ?";
            Comment comment = dbOps.executeQueryForSingleResult(sql, new Object[]{id}, this::mapResultSetToComment);
            return Optional.ofNullable(comment);
        } catch (SQLException e) {
            throw DatabaseException.readError("comment", id, e);
        }
    }

    public List<Comment> findAll() {
        try {
            String sql = "SELECT * FROM comments ORDER BY created_at DESC";
            return dbOps.executeQuery(sql, null, this::mapResultSetToComment);
        } catch (SQLException e) {
            throw DatabaseException.createError("read_all", "comment", e);
        }
    }

    public List<Comment> findByMemoryId(Long memoryId) {
        try {
            String sql = "SELECT * FROM comments WHERE memory_id = ? ORDER BY created_at ASC";
            return dbOps.executeQuery(sql, new Object[]{memoryId}, this::mapResultSetToComment);
        } catch (SQLException e) {
            throw DatabaseException.readError("comments_by_memory", memoryId, e);
        }
    }

    public List<Comment> findByAuthorId(Long authorId) {
        try {
            String sql = "SELECT * FROM comments WHERE author_id = ? ORDER BY created_at DESC";
            return dbOps.executeQuery(sql, new Object[]{authorId}, this::mapResultSetToComment);
        } catch (SQLException e) {
            throw DatabaseException.readError("comments_by_author", authorId, e);
        }
    }

    public Long save(Comment comment, Long memoryId) {
        try {
            String sql = "INSERT INTO comments (author_id, memory_id, content, created_at) " +
                    "VALUES (?, ?, ?, ?)";

            Object[] params = new Object[]{
                    comment.getAuthor().getId(),
                    memoryId,
                    comment.getContent(),
                    Timestamp.valueOf(comment.getCreatedAt() != null ?
                            comment.getCreatedAt() : LocalDateTime.now())
            };

            return dbOps.executeInsertAndGetId(sql, params);
        } catch (SQLException e) {
            throw DatabaseException.writeError("comment", e);
        }
    }

    public boolean update(Comment comment) {
        try {
            String sql = "UPDATE comments SET content = ? WHERE id = ?";

            Object[] params = new Object[]{
                    comment.getContent(),
                    comment.getId()
            };

            int rowsAffected = dbOps.executeUpdate(sql, params);
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.updateError("comment", comment.getId(), e);
        }
    }

    public boolean deleteById(Long id) {
        try {
            String sql = "DELETE FROM comments WHERE id = ?";
            int rowsAffected = dbOps.executeUpdate(sql, new Object[]{id});
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.deleteError("comment", id, e);
        }
    }

    public int deleteByMemoryId(Long memoryId) {
        try {
            String sql = "DELETE FROM comments WHERE memory_id = ?";
            return dbOps.executeUpdate(sql, new Object[]{memoryId});
        } catch (SQLException e) {
            throw DatabaseException.deleteError("comments_by_memory", memoryId, e);
        }
    }
}