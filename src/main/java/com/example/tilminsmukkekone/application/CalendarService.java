package com.example.tilminsmukkekone.application;

import com.example.tilminsmukkekone.domain.classes.Event;
import com.example.tilminsmukkekone.infrastructure.repositories.*;
import com.example.tilminsmukkekone.util.Exceptions.EventNotFoundException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    EventDB eventDB;
    UserDB userDB;
    LocationDB locationDB;

    public CalendarService(EventDB eventDB, UserDB userDB, LocationDB locationDB) {
        this.eventDB = eventDB;
        this.userDB = userDB;
        this.locationDB = locationDB;
    }

    public List<Event> getEventsForWeek(LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return eventDB.findEventsByWeek(weekStart);
    }

    public List<Event> getEventsForMonth(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Måned skal være mellem 1 og 12");
        }

        return eventDB.findEventsByMonth(month, year);
    }

    public List<Event> getEventsForYear(int year) {
        return eventDB.findEventsByYear(year);
    }

    public List<Event> getFilteredEvents(String filterType, LocalDate date, Integer year) {
        LocalDate referenceDate = date != null ? date : LocalDate.now();

        switch (filterType.toLowerCase()) {
            case "weekly":
                return getEventsForWeek(referenceDate);

            case "monthly":
                int month = referenceDate.getMonthValue();
                int yearValue = year != null ? year : referenceDate.getYear();
                return getEventsForMonth(month, yearValue);

            case "yearly":
                return getEventsForYear(year != null ? year : referenceDate.getYear());

            default:
                LocalDate now = LocalDate.now();
                return getEventsForMonth(now.getMonthValue(), now.getYear());
        }
    }

    public Map<String, Object> getDateRangeForFilter(String filterType, LocalDate referenceDate) {
        Map<String, Object> result = new HashMap<>();
        LocalDate start, end;
        String displayTitle;

        switch (filterType.toLowerCase()) {
            case "weekly":
                start = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                end = start.plusDays(6);
                displayTitle = "Uge " + start.get(ChronoField.ALIGNED_WEEK_OF_YEAR) + ", " + start.getYear();
                break;

            case "monthly":
                start = referenceDate.withDayOfMonth(1);
                end = start.plusMonths(1).minusDays(1);
                displayTitle = start.getMonth().getDisplayName(TextStyle.FULL, new Locale("da", "DK")) + " " + start.getYear();
                break;

            case "yearly":
                start = LocalDate.of(referenceDate.getYear(), 1, 1);
                end = LocalDate.of(referenceDate.getYear(), 12, 31);
                displayTitle = "År " + start.getYear();
                break;

            default:
                start = referenceDate.withDayOfMonth(1);
                end = start.plusMonths(1).minusDays(1);
                displayTitle = start.getMonth().getDisplayName(TextStyle.FULL, new Locale("da", "DK")) + " " + start.getYear();
        }

        result.put("startDate", start);
        result.put("endDate", end);
        result.put("displayTitle", displayTitle);

        return result;
    }

    public List<Event> getUpcomingEvents (int limit) {
        List<Event> events = eventDB.findUpcomingEvents();
        return events.stream().limit(limit).collect(Collectors.toList());
    }

    public Event createEvent (Event event) {
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Begivenhedens titel må ikke være tom");
        }

        Long eventId = eventDB.save(event);
        return eventDB.findById(eventId).orElseThrow(() -> new RuntimeException("Kunne ikke hente den gemte begivenhed"));
    }

    public boolean updateEvent (Event event) {
        if (event.getId() == null) {
            throw new IllegalArgumentException("Begivenhedens ID må ikke være null ved opdatering");
        }

        return eventDB.update(event);
    }

    public boolean deleteEvent (Event event) {
        if (event.getId() == null) {
            throw new IllegalArgumentException("Begivenhedens ID må ikke være null ved sletning");
        }
        return eventDB.deleteById(event.getId());
    }

    public Event getEventById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Begivenhedens ID kan ikke være null");
        }

        return eventDB.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }
}
