package com.example.tilminsmukkekone.presentation;

import com.example.tilminsmukkekone.application.MemoryService;
import com.example.tilminsmukkekone.domain.classes.Memory;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.domain.enums.MemoryType;
import com.example.tilminsmukkekone.util.Exceptions.MemoryNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/memories")
public class MemoryController {

    @Autowired
    private MemoryService memoryService;

    @GetMapping
    public String showMemoriesPage(Model model, HttpSession session) {
        // Check if user is logged in
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Load all memories from database using the existing service method
        List<Memory> memories = memoryService.getMemoriesByCreator(currentUser.getId());
        model.addAttribute("memories", memories);
        model.addAttribute("memoryTypes", MemoryType.values());

        return "memories";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createMemory(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("date") String dateStr,
            @RequestParam("type") String typeStr,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session) {

        try {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
            }

            // Parse the date
            LocalDateTime date = LocalDateTime.parse(dateStr + "T00:00:00");

            // Create memory object
            Memory memory = new Memory();
            memory.setTitle(title);
            memory.setDescription(description);
            memory.setDateOfEvent(date);
            memory.setMemoryType(MemoryType.valueOf(typeStr));
            memory.setCreator(currentUser);
            memory.setImagePaths(new ArrayList<>()); // Initialize empty list for images

            // Use the existing service method to create the memory
            Memory savedMemory = memoryService.createMemory(memory);

            // Process images using the existing service method
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    // In a real application, you would save the file to disk or cloud storage
                    // and then get the URL/path back
                    String imagePath = "/uploads/" + image.getOriginalFilename();

                    // Use the existing service method to add the image
                    memoryService.addImageToMemory(savedMemory.getId(), imagePath);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("memoryId", savedMemory.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteMemory(@PathVariable Long id, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
            }

            try {
                // Use the existing service method to retrieve the memory
                Memory memory = memoryService.getMemoryById(id);

                // Check if current user is the creator
                if (!memory.getCreator().getId().equals(currentUser.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this memory");
                }

                // Use the existing service method to delete the memory
                boolean deleted = memoryService.deleteMemory(id);

                Map<String, Object> response = new HashMap<>();
                response.put("success", deleted);

                return ResponseEntity.ok(response);
            } catch (MemoryNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Memory not found");
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}