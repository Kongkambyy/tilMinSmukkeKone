package com.example.tilminsmukkekone.domain.classes;

import java.time.LocalDateTime;

public class Comment {
    private Long id;
    private User author;
    private String content;
    private LocalDateTime createdAt;

    public Comment(){}

    public Comment(Long id, User author, String content, LocalDateTime createdAt) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
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
}
