package com.example.tilminsmukkekone.domain.classes;

import com.example.tilminsmukkekone.domain.enums.MessageType;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private User sender;
    private User recipient;
    private String subject;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledFor;
    private boolean isRead;
    private MessageType type;

    public Message() {}

    public Message (Long id, User sender, User recipient, String subject, String content, LocalDateTime createdAt, LocalDateTime scheduledFor, MessageType type) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.createdAt = createdAt;
        this.scheduledFor = scheduledFor;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
