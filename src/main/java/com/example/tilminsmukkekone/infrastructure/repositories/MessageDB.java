package com.example.tilminsmukkekone.infrastructure.repositories;

import com.example.tilminsmukkekone.domain.classes.Message;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.domain.enums.MessageType;
import com.example.tilminsmukkekone.infrastructure.util.DatabaseException;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class MessageDB {

    private final DatabaseOperations dbOps;
    private final UserDB userDB;

    public MessageDB(DatabaseOperations dbOps, UserDB userDB) {
        this.dbOps = dbOps;
        this.userDB = userDB;
    }

    private Message mapResultSetToMessage(ResultSet rs) {
        try {
            Message message = new Message();
            message.setId(rs.getLong("id"));
            message.setSubject(rs.getString("subject"));
            message.setContent(rs.getString("content"));
            message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            Timestamp scheduledFor = rs.getTimestamp("scheduled_for");
            if (scheduledFor != null) {
                message.setScheduledFor(scheduledFor.toLocalDateTime());
            }

            Long senderId = rs.getLong("sender_id");
            Long recipientId = rs.getLong("recipient_id");

            userDB.findById(senderId).ifPresent(message::setSender);
            userDB.findById(recipientId).ifPresent(message::setRecipient);

            try {
                MessageType type = MessageType.valueOf(rs.getString("message_type"));
                message = new Message(
                        message.getId(),
                        message.getSender(),
                        message.getRecipient(),
                        message.getSubject(),
                        message.getContent(),
                        message.getCreatedAt(),
                        message.getScheduledFor(),
                        type
                );
            } catch (IllegalArgumentException e) {
                // Handle invalid message type
                throw new DatabaseException("Invalid message type", "mapping", "message", e);
            }

            return message;
        } catch (SQLException e) {
            throw new DatabaseException("Error mapping database row to Message object",
                    "mapping", "message", e);
        }
    }

    /**
     * Find a message by ID.
     */
    public Optional<Message> findById(Long id) {
        try {
            String sql = "SELECT * FROM messages WHERE id = ?";
            Message message = dbOps.executeQueryForSingleResult(sql, new Object[]{id}, this::mapResultSetToMessage);
            return Optional.ofNullable(message);
        } catch (SQLException e) {
            throw DatabaseException.readError("message", id, e);
        }
    }

    /**
     * Find all messages.
     */
    public List<Message> findAll() {
        try {
            String sql = "SELECT * FROM messages ORDER BY created_at DESC";
            return dbOps.executeQuery(sql, null, this::mapResultSetToMessage);
        } catch (SQLException e) {
            throw DatabaseException.createError("read_all", "message", e);
        }
    }

    /**
     * Find messages sent by a user.
     */
    public List<Message> findBySenderId(Long senderId) {
        try {
            String sql = "SELECT * FROM messages WHERE sender_id = ? ORDER BY created_at DESC";
            return dbOps.executeQuery(sql, new Object[]{senderId}, this::mapResultSetToMessage);
        } catch (SQLException e) {
            throw DatabaseException.readError("messages_by_sender", senderId, e);
        }
    }

    /**
     * Find messages received by a user.
     */
    public List<Message> findByRecipientId(Long recipientId) {
        try {
            String sql = "SELECT * FROM messages WHERE recipient_id = ? ORDER BY created_at DESC";
            return dbOps.executeQuery(sql, new Object[]{recipientId}, this::mapResultSetToMessage);
        } catch (SQLException e) {
            throw DatabaseException.readError("messages_by_recipient", recipientId, e);
        }
    }

    /**
     * Find unread messages for a recipient.
     */
    public List<Message> findUnreadByRecipientId(Long recipientId) {
        try {
            String sql = "SELECT * FROM messages WHERE recipient_id = ? AND is_read = false ORDER BY created_at DESC";
            return dbOps.executeQuery(sql, new Object[]{recipientId}, this::mapResultSetToMessage);
        } catch (SQLException e) {
            throw DatabaseException.readError("unread_messages", recipientId, e);
        }
    }

    /**
     * Find messages by type.
     */
    public List<Message> findByType(MessageType type) {
        try {
            String sql = "SELECT * FROM messages WHERE message_type = ? ORDER BY created_at DESC";
            return dbOps.executeQuery(sql, new Object[]{type.name()}, this::mapResultSetToMessage);
        } catch (SQLException e) {
            throw DatabaseException.readError("messages_by_type", type.name(), e);
        }
    }

    /**
     * Find scheduled messages that are due to be sent.
     */
    public List<Message> findDueScheduledMessages() {
        try {
            String sql = "SELECT * FROM messages WHERE scheduled_for <= NOW() AND scheduled_for IS NOT NULL";
            return dbOps.executeQuery(sql, null, this::mapResultSetToMessage);
        } catch (SQLException e) {
            throw DatabaseException.readError("due_scheduled_messages", "now", e);
        }
    }

    /**
     * Find future scheduled messages.
     */
    public List<Message> findFutureScheduledMessages() {
        try {
            String sql = "SELECT * FROM messages WHERE scheduled_for > NOW() AND scheduled_for IS NOT NULL ORDER BY scheduled_for ASC";
            return dbOps.executeQuery(sql, null, this::mapResultSetToMessage);
        } catch (SQLException e) {
            throw DatabaseException.readError("future_scheduled_messages", "future", e);
        }
    }

    /**
     * Save a new message.
     *
     * @return the newly generated message ID
     */
    public Long save(Message message) {
        try {
            String sql = "INSERT INTO messages (sender_id, recipient_id, subject, content, " +
                    "created_at, scheduled_for, is_read, message_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            Object[] params = new Object[]{
                    message.getSender().getId(),
                    message.getRecipient().getId(),
                    message.getSubject(),
                    message.getContent(),
                    Timestamp.valueOf(message.getCreatedAt() != null ?
                            message.getCreatedAt() : LocalDateTime.now()),
                    message.getScheduledFor() != null ?
                            Timestamp.valueOf(message.getScheduledFor()) : null,
                    false, // Default to unread for new messages
                    message.getType() != null ? message.getType().name() : MessageType.GENERAL.name()
            };

            return dbOps.executeInsertAndGetId(sql, params);
        } catch (SQLException e) {
            throw DatabaseException.writeError("message", e);
        }
    }

    /**
     * Update an existing message.
     *
     * @return true if the message was updated, false otherwise
     */
    public boolean update(Message message) {
        try {
            String sql = "UPDATE messages SET sender_id = ?, recipient_id = ?, subject = ?, " +
                    "content = ?, scheduled_for = ?, message_type = ? " +
                    "WHERE id = ?";

            Object[] params = new Object[]{
                    message.getSender().getId(),
                    message.getRecipient().getId(),
                    message.getSubject(),
                    message.getContent(),
                    message.getScheduledFor() != null ?
                            Timestamp.valueOf(message.getScheduledFor()) : null,
                    message.getType().name(),
                    message.getId()
            };

            int rowsAffected = dbOps.executeUpdate(sql, params);
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.updateError("message", message.getId(), e);
        }
    }

    /**
     * Mark a message as read.
     *
     * @return true if the message was updated, false otherwise
     */
    public boolean markAsRead(Long messageId) {
        try {
            String sql = "UPDATE messages SET is_read = true WHERE id = ?";
            int rowsAffected = dbOps.executeUpdate(sql, new Object[]{messageId});
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.updateError("mark_message_read", messageId, e);
        }
    }

    /**
     * Delete a message by ID.
     *
     * @return true if the message was deleted, false otherwise
     */
    public boolean deleteById(Long id) {
        try {
            String sql = "DELETE FROM messages WHERE id = ?";
            int rowsAffected = dbOps.executeUpdate(sql, new Object[]{id});
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.deleteError("message", id, e);
        }
    }

    /**
     * Create a love note message.
     *
     * @return the ID of the created message
     */
    public Long createLoveNote(User sender, User recipient, String content, LocalDateTime scheduledFor) {
        Message message = new Message(
                null, // ID will be generated by database
                sender,
                recipient,
                "Love Note",
                content,
                LocalDateTime.now(),
                scheduledFor,
                MessageType.LOVE_NOTE
        );

        return save(message);
    }

    /**
     * Create a reminder message.
     *
     * @return the ID of the created message
     */
    public Long createReminder(User sender, User recipient, String subject, String content,
                               LocalDateTime scheduledFor) {
        Message message = new Message(
                null, // ID will be generated by database
                sender,
                recipient,
                subject,
                content,
                LocalDateTime.now(),
                scheduledFor,
                MessageType.REMINDER
        );

        return save(message);
    }

    /**
     * Get the count of unread messages for a recipient.
     */
    public int countUnreadMessages(Long recipientId) {
        try {
            String sql = "SELECT COUNT(*) FROM messages WHERE recipient_id = ? AND is_read = false";
            Integer count = dbOps.executeQueryForSingleResult(sql, new Object[]{recipientId}, rs -> {
                try {
                    return rs.getInt(1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return count != null ? count : 0;
        } catch (SQLException e) {
            throw DatabaseException.createError("count_unread", "message", e);
        }
    }
}