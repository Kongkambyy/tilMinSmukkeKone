package com.example.tilminsmukkekone.presentation;

import com.example.tilminsmukkekone.application.CalendarService;
import com.example.tilminsmukkekone.application.LoveService;
import com.example.tilminsmukkekone.domain.classes.Event;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.domain.enums.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/kalendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final LoveService loveService;

    @Autowired
    public CalendarController(CalendarService calendarService, LoveService loveService) {
        this.calendarService = calendarService;
        this.loveService = loveService;
    }

    @GetMapping
    public String showCalendarPage(
            @RequestParam(name = "view", defaultValue = "monthly") String viewType,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year,
            Model model,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        LocalDate now = LocalDate.now();
        LocalDate referenceDate;

        if (date != null) {
            referenceDate = date;
        } else if (month != null && year != null) {
            referenceDate = LocalDate.of(year, month, 1);
        } else if (month != null) {
            referenceDate = LocalDate.of(now.getYear(), month, 1);
        } else if (year != null) {
            referenceDate = LocalDate.of(year, now.getMonthValue(), 1);
        } else {
            referenceDate = now;
        }

        Map<String, Object> dateRange = calendarService.getDateRangeForFilter(viewType, referenceDate);
        List<Event> events = calendarService.getFilteredEvents(viewType, referenceDate, year != null ? year : referenceDate.getYear());
        List<Event> upcomingEvents = calendarService.getUpcomingEvents(5);

        if (currentUser.getAnniversary() != null) {
            LocalDate nextAnniversary = loveService.getNextAnniversaryDate(currentUser);
            model.addAttribute("anniversaryDate", nextAnniversary); // Changed from nextAnniversary to match HTML
        }

        model.addAttribute("viewType", viewType);
        model.addAttribute("currentMonth", ((LocalDate)dateRange.get("startDate")).getMonth().toString());
        model.addAttribute("currentYear", ((LocalDate)dateRange.get("startDate")).getYear());
        model.addAttribute("displayTitle", dateRange.get("displayTitle"));
        model.addAttribute("startDate", dateRange.get("startDate"));
        model.addAttribute("endDate", dateRange.get("endDate"));
        model.addAttribute("referenceDate", referenceDate);
        model.addAttribute("events", events);
        model.addAttribute("upcomingEvents", upcomingEvents);

        if (viewType.equals("monthly")) {
            LocalDate monthStart = ((LocalDate)dateRange.get("startDate"));
            model.addAttribute("daysInMonth", monthStart.lengthOfMonth());
            model.addAttribute("firstDayOfMonth", monthStart.getDayOfWeek().getValue());
        }

        return "kalendar";
    }

    // Removed duplicate /event/create endpoint since you have /event/new

    @GetMapping("/event/{id}")
    public String showEventDetails(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Event event = calendarService.getEventById(id);
        model.addAttribute("event", event);

        return "event-details";
    }

    @GetMapping("/event/new")
    public String showCreateEventForm(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("event", new Event());
        model.addAttribute("eventTypes", EventType.values());

        return "event-form";
    }

    @PostMapping("/event/new")
    public String createEvent(@ModelAttribute Event event, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        calendarService.createEvent(event);

        return "redirect:/kalendar";
    }

    @GetMapping("/event/{id}/edit")
    public String showEditEventForm(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Added code to retrieve the event
        Event event = calendarService.getEventById(id);
        model.addAttribute("event", event);
        model.addAttribute("eventTypes", EventType.values());

        return "event-form";
    }

    @PostMapping("/event/{id}/edit")
    public String updateEvent(@PathVariable Long id, @ModelAttribute Event event, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Added code to actually update the event
        event.setId(id); // Ensure the ID is set
        calendarService.updateEvent(event);

        return "redirect:/kalendar";
    }

    @PostMapping("/event/{id}/delete")
    public String deleteEvent(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Added code to actually delete the event
        calendarService.deleteEvent(new Event(id, null, null, null, null, null, null, null));

        return "redirect:/kalendar";
    }
}