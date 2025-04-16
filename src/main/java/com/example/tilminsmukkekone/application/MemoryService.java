package com.example.tilminsmukkekone.application;

import com.example.tilminsmukkekone.domain.classes.Comment;
import com.example.tilminsmukkekone.domain.classes.Memory;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.domain.enums.MemoryType;
import com.example.tilminsmukkekone.infrastructure.repositories.CommentDB;
import com.example.tilminsmukkekone.infrastructure.repositories.MemoryDB;
import com.example.tilminsmukkekone.infrastructure.repositories.UserDB;
import com.example.tilminsmukkekone.infrastructure.util.ServiceException;
import com.example.tilminsmukkekone.util.Exceptions.MemoryNotFoundException;
import com.example.tilminsmukkekone.util.Exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MemoryService {

    MemoryDB memoryDB;
    UserDB userDB;
    CommentDB commentDB;

    // Konstruktør til dependency injection
    public MemoryService(MemoryDB memoryDB, UserDB userDB, CommentDB commentDB) {
        this.memoryDB = memoryDB;
        this.userDB = userDB;
        this.commentDB = commentDB;
    }

    public Memory createMemory(Memory memory) {
        // Trin 1: Validér at påkrævede felter er udfyldt
        if (memory.getTitle() == null || memory.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Titel må ikke være tom");
        }

        if (memory.getDateOfEvent() == null) {
            throw new IllegalArgumentException("Dato for minde må ikke være tom");
        }

        if (memory.getMemoryType() == null) {
            throw new IllegalArgumentException("Mindetype må ikke være tom");
        }

        if (memory.getCreator() == null || memory.getCreator().getId() == null) {
            throw new IllegalArgumentException("Skaberen af mindet skal angives");
        }

        // Trin 2: Kontrollér at skaberen eksisterer
        Optional<User> creator = userDB.findById(memory.getCreator().getId());
        if (!creator.isPresent()) {
            throw new UserNotFoundException(memory.getCreator().getId());
        }

        // Trin 3: Sæt oprettelsestidspunkt hvis ikke angivet
        if (memory.getDateOfEvent() == null) {
            memory.setDateOfEvent(LocalDateTime.now());
        }

        // Trin 4: Initialiser billedsti-liste hvis den er null
        if (memory.getImagePaths() == null) {
            memory.setImagePaths(new ArrayList<>());
        }

        // Trin 5: Initialiser kommentarlisten hvis den er null
        if (memory.getComments() == null) {
            memory.setComments(new ArrayList<>());
        }

        // Trin 6: Gem mindet i databasen
        Long memoryId = memoryDB.save(memory);

        // Trin 7: Hent og returnér det gemte minde
        return memoryDB.findById(memoryId)
                .orElseThrow(() -> new ServiceException("Kunne ikke hente det gemte minde"));
    }

    public Memory getMemoryById(Long memoryId) throws MemoryNotFoundException {
        // Trin 1: Hent mindet fra databasen
        return memoryDB.findById(memoryId)
                // Trin 2: Hvis mindet ikke findes, kast en passende undtagelse
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));
    }

    public Memory updateMemory(Memory memory) throws MemoryNotFoundException {
        // Trin 1: Kontrollér at mindet har et ID
        if (memory.getId() == null) {
            throw new IllegalArgumentException("Minde-ID skal være angivet for at opdatere");
        }

        // Trin 2: Hent det eksisterende minde
        Memory existingMemory = memoryDB.findById(memory.getId())
                .orElseThrow(() -> new MemoryNotFoundException(memory.getId()));

        // Trin 3: Opdatér ikke-null felter
        if (memory.getTitle() != null) {
            existingMemory.setTitle(memory.getTitle());
        }

        if (memory.getDescription() != null) {
            existingMemory.setDescription(memory.getDescription());
        }

        if (memory.getDateOfEvent() != null) {
            existingMemory.setDateOfEvent(memory.getDateOfEvent());
        }

        if (memory.getLocation() != null) {
            existingMemory.setLocation(memory.getLocation());
        }

        if (memory.getMemoryType() != null) {
            existingMemory.setMemoryType(memory.getMemoryType());
        }

        // Trin 4: Opdatér billedstier hvis angivet
        if (memory.getImagePaths() != null) {
            existingMemory.setImagePaths(memory.getImagePaths());
        }

        // Trin 5: Gem det opdaterede minde
        boolean updateSuccess = memoryDB.update(existingMemory);

        // Trin 6: Kontrollér om opdateringen lykkedes
        if (!updateSuccess) {
            throw new ServiceException("Kunne ikke opdatere mindet");
        }

        // Trin 7: Returnér det opdaterede minde
        return existingMemory;
    }

    public boolean deleteMemory(Long memoryId) throws MemoryNotFoundException {
        // Trin 1: Kontrollér at mindet eksisterer
        if (!memoryDB.findById(memoryId).isPresent()) {
            throw new MemoryNotFoundException(memoryId);
        }

        // Trin 2: Slet alle kommentarer knyttet til mindet
        commentDB.deleteByMemoryId(memoryId);

        // Trin 3: Slet mindet fra databasen
        return memoryDB.deleteById(memoryId);
    }

    public List<Memory> getMemoriesByCreator(Long userId) {
        // Trin 1: Validér bruger-ID
        if (userId == null) {
            throw new IllegalArgumentException("Bruger-ID må ikke være null");
        }

        // Trin 2: Kontrollér om brugeren eksisterer
        if (!userDB.findById(userId).isPresent()) {
            throw new UserNotFoundException(userId);
        }

        // Trin 3: Hent og returnér alle minder oprettet af brugeren
        return memoryDB.findByCreatorId(userId);
    }

    public List<Memory> getMemoriesByType(MemoryType type) {
        // Trin 1: Validér type
        if (type == null) {
            throw new IllegalArgumentException("Mindetype må ikke være null");
        }

        // Trin 2: Hent og returnér alle minder af den angivne type
        return memoryDB.findByMemoryType(type);
    }

    public boolean addImageToMemory(Long memoryId, String imagePath) throws MemoryNotFoundException {
        // Trin 1: Validér input
        if (memoryId == null || imagePath == null || imagePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Minde-ID og billedsti skal angives");
        }

        // Trin 2: Hent mindet
        Memory memory = memoryDB.findById(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));

        // Trin 3: Initialiser billedsti-listen hvis den er null
        if (memory.getImagePaths() == null) {
            memory.setImagePaths(new ArrayList<>());
        }

        // Trin 4: Tjek om billedet allerede er tilføjet
        if (memory.getImagePaths().contains(imagePath)) {
            // Billedet er allerede tilføjet, betragt dette som en succes
            return true;
        }

        // Trin 5: Tilføj billedsti til mindet
        memory.getImagePaths().add(imagePath);

        // Trin 6: Gem ændringerne
        boolean updateSuccess = memoryDB.update(memory);

        // Trin 7: Returnér resultatet af opdateringen
        return updateSuccess;
    }

    public boolean removeImageFromMemory(Long memoryId, String imagePath) throws MemoryNotFoundException {
        // Trin 1: Validér input
        if (memoryId == null || imagePath == null || imagePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Minde-ID og billedsti skal angives");
        }

        // Trin 2: Hent mindet
        Memory memory = memoryDB.findById(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));

        // Trin 3: Kontrollér om mindet har billeder
        if (memory.getImagePaths() == null || memory.getImagePaths().isEmpty()) {
            // Ingen billeder at fjerne, betragt dette som en succes
            return true;
        }

        // Trin 4: Fjern billedstien hvis den findes
        boolean removed = memory.getImagePaths().remove(imagePath);

        // Trin 5: Hvis billedet ikke var i listen, intet behov for at opdatere
        if (!removed) {
            return true;
        }

        // Trin 6: Gem ændringerne
        boolean updateSuccess = memoryDB.update(memory);

        // Trin 7: Returnér resultatet af opdateringen
        return updateSuccess;
    }

    public List<Memory> getMemoriesByEvent(Long eventId) {
        // Trin 1: Validér event-ID
        if (eventId == null) {
            throw new IllegalArgumentException("Event-ID må ikke være null");
        }

        // Trin 2: Hent og returnér alle minder knyttet til dette event
        return memoryDB.findByEventId(eventId);
    }

    public Comment addCommentToMemory(Long memoryId, Comment comment) throws MemoryNotFoundException {
        // Trin 1: Validér input
        if (memoryId == null || comment == null) {
            throw new IllegalArgumentException("Minde-ID og kommentar skal angives");
        }

        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Kommentarindhold må ikke være tomt");
        }

        if (comment.getAuthor() == null || comment.getAuthor().getId() == null) {
            throw new IllegalArgumentException("Kommentarforfatteren skal angives");
        }

        // Trin 2: Kontrollér at mindet eksisterer
        if (!memoryDB.findById(memoryId).isPresent()) {
            throw new MemoryNotFoundException(memoryId);
        }

        // Trin 3: Kontrollér at forfatteren eksisterer
        if (!userDB.findById(comment.getAuthor().getId()).isPresent()) {
            throw new UserNotFoundException(comment.getAuthor().getId());
        }

        // Trin 4: Sæt oprettelsestidspunkt hvis ikke angivet
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }

        // Trin 5: Gem kommentaren i databasen
        Long commentId = commentDB.save(comment, memoryId);

        // Trin 6: Hent og returnér den gemte kommentar
        return commentDB.findById(commentId)
                .orElseThrow(() -> new ServiceException("Kunne ikke hente den gemte kommentar"));
    }

    public List<Comment> getCommentsForMemory(Long memoryId) throws MemoryNotFoundException {
        // Trin 1: Validér minde-ID
        if (memoryId == null) {
            throw new IllegalArgumentException("Minde-ID må ikke være null");
        }

        // Trin 2: Kontrollér at mindet eksisterer
        if (!memoryDB.findById(memoryId).isPresent()) {
            throw new MemoryNotFoundException(memoryId);
        }

        // Trin 3: Hent og returnér alle kommentarer for dette minde
        return commentDB.findByMemoryId(memoryId);
    }
}