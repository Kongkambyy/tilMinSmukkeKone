package com.example.tilminsmukkekone.presentation;

import com.example.tilminsmukkekone.domain.classes.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    @GetMapping
    public String showQuizPage(Model model, HttpSession session) {
        // Check if user is logged in
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Add user's name to model
        model.addAttribute("userName", currentUser.getName());

        // Add placeholder quiz data
        model.addAttribute("quizReady", false);
        model.addAttribute("pageTitle", "Quiz om vores forhold");

        return "quiz";
    }
}