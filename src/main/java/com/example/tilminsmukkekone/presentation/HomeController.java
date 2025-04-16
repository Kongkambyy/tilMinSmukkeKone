package com.example.tilminsmukkekone.presentation;

import com.example.tilminsmukkekone.application.UserService;
import com.example.tilminsmukkekone.domain.classes.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        boolean isLoggedIn = (session.getAttribute("currentUser") != null);
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            User user = (User) session.getAttribute("currentUser");
            model.addAttribute("userName", user.getName());
        }

        return "Body";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        // Redirect to home if already logged in
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password,
                               HttpSession session) {
        try {
            User user = userService.authenticate(username, password);
            session.setAttribute("currentUser", user);
            System.out.println("Login success for user: " + username);
            return "redirect:/";
        } catch (Exception e) {
            System.out.println("Login failed for user: " + username + " - " + e.getMessage());
            return "redirect:/login";
        }
    }
}