package com.example.tilminsmukkekone.domain.classes;

import com.example.tilminsmukkekone.domain.enums.EventType;

import java.time.LocalDateTime;
import java.util.List;

public class Event {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private EventType type;
    private Location location;
    private List<User> participants;
    private List<Memory> relatedMemories;

    public Event() {}

    public Event (Long id, String title, String description, LocalDateTime eventDate, EventType type, Location location, List<User> participants, List<Memory> relatedMemories) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.type = type;
        this.location = location;
        this.participants = participants;
        this.relatedMemories = relatedMemories;
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

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public List<Memory> getRelatedMemories() {
        return relatedMemories;
    }

    public void setRelatedMemories(List<Memory> relatedMemories) {
        this.relatedMemories = relatedMemories;
    }
}
