package com.example.tilminsmukkekone.application;

import com.example.tilminsmukkekone.domain.classes.Event;
import com.example.tilminsmukkekone.domain.classes.Location;
import com.example.tilminsmukkekone.domain.classes.Memory;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.domain.enums.EventType;
import com.example.tilminsmukkekone.infrastructure.repositories.EventDB;
import com.example.tilminsmukkekone.infrastructure.repositories.LocationDB;
import com.example.tilminsmukkekone.infrastructure.repositories.MemoryDB;
import com.example.tilminsmukkekone.infrastructure.repositories.UserDB;
import com.example.tilminsmukkekone.util.Exceptions.EventNotFoundException;
import com.example.tilminsmukkekone.util.Exceptions.MemoryNotFoundException;
import com.example.tilminsmukkekone.util.Exceptions.PermissionDeniedException;
import com.example.tilminsmukkekone.util.Exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventService {

    EventDB eventDB;
    UserDB userDB;
    MemoryDB memoryDB;
    LocationDB locationDB;

    public Event createEvent(Event event) {

        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Begivenheden kan ikke være tom");
        }

        if (event.getEventDate() == null) {
            throw new IllegalArgumentException("Datoen kan ikke være tom");
        }

        if (event.getType() == null) {
            throw new IllegalArgumentException("Typen af begivenhed kan ikke være tom");
        }

        if (event.getLocation() != null) {
            Long locationId = locationDB.save(event.getLocation());

            event.getLocation().setId(locationId);
        }

        Long eventId = eventDB.save(event);

        return eventDB.findById(eventId).orElseThrow(() -> new RuntimeException("Kunne ikke hente den gemte behivenhed"));
    }

    public Event getEventById(Long eventId) throws EventNotFoundException {
        return eventDB.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
    }

    public Event updateEvent(Long eventId, Event updatedEvent) throws EventNotFoundException, PermissionDeniedException {
        Event existingEvent = eventDB.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (updatedEvent.getTitle() != null) {
            existingEvent.setTitle(updatedEvent.getTitle());
        }

        if (updatedEvent.getDescription() != null) {
            existingEvent.setDescription(updatedEvent.getDescription());
        }

        if (updatedEvent.getEventDate() != null) {
            existingEvent.setEventDate(updatedEvent.getEventDate());
        }

        if (updatedEvent.getLocation() != null) {

            if (existingEvent.getLocation() != null) {
                updatedEvent.getLocation().setId(existingEvent.getLocation().getId());
                locationDB.update(updatedEvent.getLocation());
                existingEvent.setLocation(updatedEvent.getLocation());
            }
        }

        if (updatedEvent.getParticipants() != null) {
            existingEvent.setParticipants(updatedEvent.getParticipants());
        }

        if (updatedEvent.getRelatedMemories() != null) {
            existingEvent.setRelatedMemories(updatedEvent.getRelatedMemories());
        }

        boolean updateSuccess = eventDB.update(existingEvent);

        if (!updateSuccess) {
            throw new PermissionDeniedException("Ikke tilladelse");
        }

        return existingEvent;
    }

    public boolean deleteEvent(Long eventId) throws EventNotFoundException, PermissionDeniedException {
        if (eventDB.findById(eventId).isEmpty()) {
            throw new EventNotFoundException(eventId);
        }

        return eventDB.deleteById(eventId);
    }

    public List<Event> getUpcomingEvents() {
        return eventDB.findUpcomingEvents();
    }

    public List<Event> getEventsByType(EventType eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("Begivenhed kan ikke være tomt");
        }

        return eventDB.findByEventType(eventType);
    }

    public List<Event> getEventsByParticipant(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Ingen brugere");
        }

        return eventDB.findByParticipantId(userId);
    }

    public boolean addParticipantToEvent(Long eventId, Long userId) throws EventNotFoundException, UserNotFoundException {

        Event event = eventDB.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        User user = userDB.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (event.getParticipants() != null && event.getParticipants().stream()
                .anyMatch(participant -> participant.getId().equals(userId))) {
            return true;
        }

        if (event.getParticipants() == null) {
            event.setParticipants(new ArrayList<>());
        }

        event.getParticipants().add(user);

        return eventDB.update(event);
    }

    public boolean removeParticipantFromEvent(Long eventId, Long userId) throws EventNotFoundException, UserNotFoundException {

        Event event = eventDB.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!userDB.findById(userId).isPresent()) {
            throw new UserNotFoundException(userId);
        }

        if (event.getParticipants() == null || event.getParticipants().isEmpty()) {
            return true;
        }

        boolean removed = event.getParticipants().removeIf(participant -> participant.getId().equals(userId));

        if (!removed) {
            return true;
        }

        return eventDB.update(event);
    }

    public boolean linkMemoryToEvent(Long eventId, Long memoryId) throws EventNotFoundException, MemoryNotFoundException {

        Event event = eventDB.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Memory memory = memoryDB.findById(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));

        if (event.getRelatedMemories() != null && event.getRelatedMemories().stream()
                .anyMatch(relatedMemory -> relatedMemory.getId().equals(memoryId))) {
            return true;
        }

        if (event.getRelatedMemories() == null) {
            event.setRelatedMemories(new ArrayList<>());
        }

        event.getRelatedMemories().add(memory);

        return eventDB.update(event);
    }

    public boolean unlinkMemoryFromEvent(Long eventId, Long memoryId) throws EventNotFoundException, MemoryNotFoundException {

        Event event = eventDB.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!memoryDB.findById(memoryId).isPresent()) {
            throw new MemoryNotFoundException(memoryId);
        }

        if (event.getRelatedMemories() == null || event.getRelatedMemories().isEmpty()) {
            return true;
        }

        boolean removed = event.getRelatedMemories().removeIf(memory -> memory.getId().equals(memoryId));

        if (!removed) {
            return true;
        }

        return eventDB.update(event);
    }

    public boolean setEventLocation(Long eventId, Location location) throws EventNotFoundException {

        Event event = eventDB.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Long locationId;
        if (location.getId() != null) {
            //
            boolean updated = locationDB.update(location);
            if (!updated) {
                throw new RuntimeException("Kunne ikke opdatere lokation");
            }
            locationId = location.getId();
        } else {
            locationId = locationDB.save(location);
            location.setId(locationId);
        }

        event.setLocation(location);

        return eventDB.update(event);
    }

    public List<Event> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start og slut dag kan ikke være null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Startdato kan ikke være efter slutdato");
        }

        return eventDB.findAll().stream()
                .filter(event -> !event.getEventDate().isBefore(startDate) &&
                        !event.getEventDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
}