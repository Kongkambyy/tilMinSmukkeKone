package com.example.tilminsmukkekone.presentation;

import com.example.tilminsmukkekone.application.UserService;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.util.Exceptions.InvalidCredentialsException;
import com.example.tilminsmukkekone.util.Exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "Body";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.authenticate(username, password);

            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("loggedIn", true);

            return "redirect:/memories";

        } catch (UserNotFoundException | InvalidCredentialsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/memories")
    public String memories() {
        return "memories";
    }

    @GetMapping("/quiz")
    public String quiz() {
        return "quiz";
    }

    @GetMapping("/calendar")
    public String calendar() {
        return "calendar";
    }

    @GetMapping("/photos")
    public String photos() {
        return "photos";
    }

    @GetMapping("/messages")
    public String messages() {
        return "messages";
    }
}