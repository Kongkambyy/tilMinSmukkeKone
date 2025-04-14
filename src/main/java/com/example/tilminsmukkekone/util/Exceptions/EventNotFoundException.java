package com.example.tilminsmukkekone.util.Exceptions;

public class EventNotFoundException extends RuntimeException {
  public EventNotFoundException(Long eventId) {
    super("Event with ID " + eventId + " not found");
  }
}