package com.example.tilminsmukkekone.presentation;

import com.example.tilminsmukkekone.domain.classes.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/kalendar")
public class CalendarController {

    @GetMapping
    public String showCalendarPage(Model model, HttpSession session) {
        // Check if user is logged in
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get current year and month
        YearMonth currentYearMonth = YearMonth.now();

        // Add calendar data to model
        model.addAttribute("currentMonth", currentYearMonth.getMonth().toString());
        model.addAttribute("currentYear", currentYearMonth.getYear());
        model.addAttribute("daysInMonth", currentYearMonth.lengthOfMonth());
        model.addAttribute("firstDayOfMonth", currentYearMonth.atDay(1).getDayOfWeek().getValue());

        // Add user's anniversary if available
        if (currentUser.getAnniversary() != null) {
            LocalDate nextAnniversary = getNextAnnualDate(currentUser.getAnniversary());
            model.addAttribute("anniversaryDate", nextAnniversary);
        }

        // Placeholder for events
        model.addAttribute("events", new ArrayList<>());

        return "kalendar";
    }

    /**
     * Get the next occurrence of a date (for anniversaries)
     */
    private LocalDate getNextAnnualDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate nextOccurrence = date.withYear(today.getYear());

        if (nextOccurrence.isBefore(today) || nextOccurrence.isEqual(today)) {
            nextOccurrence = nextOccurrence.plusYears(1);
        }

        return nextOccurrence;
    }
}