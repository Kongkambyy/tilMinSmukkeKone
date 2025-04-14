package com.example.tilminsmukkekone.domain.classes;

import com.example.tilminsmukkekone.domain.enums.MemoryType;

import java.time.LocalDateTime;
import java.util.List;

public class Memory {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dateOfEvent;
    private String location;
    private List<String> imagePaths;
    private MemoryType memoryType;
    private User creator;
    private List<Comment> comments;

    public Memory() {
    }

    public Memory(Long id, String title, String description, LocalDateTime dateOfEvent, String location, List<String> imagePaths, MemoryType memoryType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateOfEvent = dateOfEvent;
        this.location = location;
        this.imagePaths = imagePaths;
        this.memoryType = memoryType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateOfEvent() {
        return dateOfEvent;
    }

    public void setDateOfEvent(LocalDateTime dateOfEvent) {
        this.dateOfEvent = dateOfEvent;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(MemoryType memoryType) {
        this.memoryType = memoryType;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
