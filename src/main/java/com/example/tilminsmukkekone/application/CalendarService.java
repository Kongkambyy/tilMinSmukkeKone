package com.example.tilminsmukkekone.application;

import com.example.tilminsmukkekone.domain.classes.Event;
import com.example.tilminsmukkekone.infrastructure.repositories.*;

import java.util.List;


public class CalendarService {

    EventDB eventDB;
    UserDB userDB;
    LocationDB locationDB;

    public CalendarService() {
        this.eventDB = eventDB;
    }

    public List<Event> getEventsForMonth(int month, int year) {
        return eventDB.findUpcomingEventsWithinAMonth();
    }



}
