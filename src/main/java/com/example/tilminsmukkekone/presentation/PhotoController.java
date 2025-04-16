package com.example.tilminsmukkekone.presentation;

import com.example.tilminsmukkekone.domain.classes.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/billeder")
public class PhotoController {

    @GetMapping
    public String showPhotosPage(Model model, HttpSession session) {
        // Check if user is logged in
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Add placeholder photo gallery data
        List<Map<String, String>> photoAlbums = new ArrayList<>();

        // Example album structure
        Map<String, String> album1 = new HashMap<>();
        album1.put("title", "Vores første ferie sammen");
        album1.put("date", "Juli 2018");
        album1.put("coverImage", "/images/placeholders/album1.jpg");
        album1.put("count", "24");

        Map<String, String> album2 = new HashMap<>();
        album2.put("title", "Særlige øjeblikke");
        album2.put("date", "2018-2025");
        album2.put("coverImage", "/images/placeholders/album2.jpg");
        album2.put("count", "42");

        photoAlbums.add(album1);
        photoAlbums.add(album2);

        model.addAttribute("photoAlbums", photoAlbums);
        model.addAttribute("pageTitle", "Billeder af os");

        return "billeder";
    }
}