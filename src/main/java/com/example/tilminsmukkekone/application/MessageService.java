package com.example.tilminsmukkekone.application;

import com.example.tilminsmukkekone.domain.classes.Message;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.domain.enums.MessageType;
import com.example.tilminsmukkekone.infrastructure.repositories.MessageDB;
import com.example.tilminsmukkekone.infrastructure.repositories.UserDB;
import com.example.tilminsmukkekone.infrastructure.util.ServiceException;
import com.example.tilminsmukkekone.util.Exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MessageService {

    private MessageDB messageDB;
    private UserDB userDB;

    public MessageService(MessageDB messageDB, UserDB userDB) {
        this.messageDB = messageDB;
        this.userDB = userDB;
    }

    public Message createMessage(Message message) {
        // Trin 1: Validér påkrævede felter
        if (message.getSender() == null || message.getSender().getId() == null) {
            throw new IllegalArgumentException("Afsender skal angives");
        }

        if (message.getRecipient() == null || message.getRecipient().getId() == null) {
            throw new IllegalArgumentException("Modtager skal angives");
        }

        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Beskedindhold må ikke være tomt");
        }

        // Trin 2: Verificér at afsender eksisterer
        Optional<User> sender = userDB.findById(message.getSender().getId());
        if (!sender.isPresent()) {
            throw new UserNotFoundException(message.getSender().getId());
        }

        // Trin 3: Verificér at modtager eksisterer
        Optional<User> recipient = userDB.findById(message.getRecipient().getId());
        if (!recipient.isPresent()) {
            throw new UserNotFoundException(message.getRecipient().getId());
        }

        // Trin 4: Sæt oprettelsestidspunkt hvis ikke angivet
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }

        // Trin 5: Standardisér til almindelig besked hvis type ikke er angivet
        if (message.getType() == null) {
            message.setType(MessageType.GENERAL);
        }

        // Trin 6: Sæt initial læsestatus
        message.setRead(false);

        // Trin 7: Gem beskeden i databasen
        Long messageId = messageDB.save(message);

        // Trin 8: Hent og returnér den gemte besked
        return messageDB.findById(messageId)
                .orElseThrow(() -> new ServiceException("Kunne ikke hente den gemte besked"));
    }

    public Message getMessageById(Long messageId) {
        // Trin 1: Validér input
        if (messageId == null) {
            throw new IllegalArgumentException("Besked-ID må ikke være null");
        }

        // Trin 2: Hent og returnér beskeden
        return messageDB.findById(messageId)
                .orElseThrow(() -> new ServiceException("Besked ikke fundet med ID: " + messageId));
    }

    public Message updateMessage(Long messageId, Message updatedMessage) {
        // Trin 1: Validér input
        if (messageId == null || updatedMessage == null) {
            throw new IllegalArgumentException("Besked-ID og opdateret besked skal angives");
        }

        // Trin 2: Hent eksisterende besked
        Message existingMessage = messageDB.findById(messageId)
                .orElseThrow(() -> new ServiceException("Besked ikke fundet med ID: " + messageId));

        // Trin 3: Opdatér ikke-null felter
        if (updatedMessage.getSubject() != null) {
            existingMessage.setSubject(updatedMessage.getSubject());
        }

        if (updatedMessage.getContent() != null) {
            existingMessage.setContent(updatedMessage.getContent());
        }

        if (updatedMessage.getScheduledFor() != null) {
            existingMessage.setScheduledFor(updatedMessage.getScheduledFor());
        }

        if (updatedMessage.getType() != null) {
            existingMessage.setType(updatedMessage.getType());
        }

        // Trin 4: Gem den opdaterede besked
        boolean updateSuccess = messageDB.update(existingMessage);

        // Trin 5: Kontrollér om opdateringen lykkedes
        if (!updateSuccess) {
            throw new ServiceException("Kunne ikke opdatere beskeden");
        }

        // Trin 6: Returnér den opdaterede besked
        return existingMessage;
    }

    public boolean deleteMessage(Long messageId) {
        // Trin 1: Validér input
        if (messageId == null) {
            throw new IllegalArgumentException("Besked-ID må ikke være null");
        }

        // Trin 2: Kontrollér om beskeden findes
        if (!messageDB.findById(messageId).isPresent()) {
            throw new ServiceException("Besked ikke fundet med ID: " + messageId);
        }

        // Trin 3: Slet beskeden
        return messageDB.deleteById(messageId);
    }

    public boolean markMessageAsRead(Long messageId) {
        // Trin 1: Validér input
        if (messageId == null) {
            throw new IllegalArgumentException("Besked-ID må ikke være null");
        }

        // Trin 2: Kontrollér om beskeden findes
        if (!messageDB.findById(messageId).isPresent()) {
            throw new ServiceException("Besked ikke fundet med ID: " + messageId);
        }

        // Trin 3: Markér beskeden som læst
        return messageDB.markAsRead(messageId);
    }

    public List<Message> getMessagesBySender(Long userId) {
        // Trin 1: Validér input
        if (userId == null) {
            throw new IllegalArgumentException("Bruger-ID må ikke være null");
        }

        // Trin 2: Kontrollér om brugeren findes
        if (!userDB.findById(userId).isPresent()) {
            throw new UserNotFoundException(userId);
        }

        // Trin 3: Hent og returnér beskeder
        return messageDB.findBySenderId(userId);
    }

    public List<Message> getMessagesByRecipient(Long userId) {
        // Trin 1: Validér input
        if (userId == null) {
            throw new IllegalArgumentException("Bruger-ID må ikke være null");
        }

        // Trin 2: Kontrollér om brugeren findes
        if (!userDB.findById(userId).isPresent()) {
            throw new UserNotFoundException(userId);
        }

        // Trin 3: Hent og returnér beskeder
        return messageDB.findByRecipientId(userId);
    }

    public List<Message> getUnreadMessages(Long userId) {
        // Trin 1: Validér input
        if (userId == null) {
            throw new IllegalArgumentException("Bruger-ID må ikke være null");
        }

        // Trin 2: Kontrollér om brugeren findes
        if (!userDB.findById(userId).isPresent()) {
            throw new UserNotFoundException(userId);
        }

        // Trin 3: Hent og returnér ulæste beskeder
        return messageDB.findUnreadByRecipientId(userId);
    }

    public int getUnreadMessageCount(Long userId) {
        // Trin 1: Validér input
        if (userId == null) {
            throw new IllegalArgumentException("Bruger-ID må ikke være null");
        }

        // Trin 2: Kontrollér om brugeren findes
        if (!userDB.findById(userId).isPresent()) {
            throw new UserNotFoundException(userId);
        }

        // Trin 3: Hent og returnér antal
        return messageDB.countUnreadMessages(userId);
    }

    public List<Message> getMessagesByType(MessageType type) {
        // Trin 1: Validér input
        if (type == null) {
            throw new IllegalArgumentException("Beskedtype må ikke være null");
        }

        // Trin 2: Hent og returnér beskeder
        return messageDB.findByType(type);
    }

    public List<Message> getDueScheduledMessages() {
        // Trin 1: Hent og returnér planlagte beskeder der skal sendes
        return messageDB.findDueScheduledMessages();
    }

    public Message scheduleMessage(Message message, LocalDateTime scheduledTime) {
        // Trin 1: Validér input
        if (message == null) {
            throw new IllegalArgumentException("Besked må ikke være null");
        }

        if (scheduledTime == null) {
            throw new IllegalArgumentException("Planlagt tidspunkt må ikke være null");
        }

        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Planlagt tidspunkt skal være i fremtiden");
        }

        // Trin 2: Sæt planlagt tidspunkt
        message.setScheduledFor(scheduledTime);

        // Trin 3: Opret beskeden
        return createMessage(message);
    }

    public Message createLoveNote(User sender, User recipient, String content, LocalDateTime scheduledFor) {
        // Trin 1: Validér input
        if (sender == null || sender.getId() == null) {
            throw new IllegalArgumentException("Afsender skal angives");
        }

        if (recipient == null || recipient.getId() == null) {
            throw new IllegalArgumentException("Modtager skal angives");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Indhold må ikke være tomt");
        }

        // Trin 2: Opret kærlighedsbesked ved hjælp af MessageDB
        Long messageId = messageDB.createLoveNote(sender, recipient, content, scheduledFor);

        // Trin 3: Hent og returnér den oprettede besked
        return messageDB.findById(messageId)
                .orElseThrow(() -> new ServiceException("Kunne ikke hente den oprettede kærlighedsbesked"));
    }

    public Message createReminder(User sender, User recipient, String subject, String content, LocalDateTime scheduledFor) {
        // Trin 1: Validér input
        if (sender == null || sender.getId() == null) {
            throw new IllegalArgumentException("Afsender skal angives");
        }

        if (recipient == null || recipient.getId() == null) {
            throw new IllegalArgumentException("Modtager skal angives");
        }

        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Emne må ikke være tomt");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Indhold må ikke være tomt");
        }

        if (scheduledFor == null) {
            throw new IllegalArgumentException("Planlagt tidspunkt skal angives for påmindelser");
        }

        // Trin 2: Opret påmindelsesbesked ved hjælp af MessageDB
        Long messageId = messageDB.createReminder(sender, recipient, subject, content, scheduledFor);

        // Trin 3: Hent og returnér den oprettede besked
        return messageDB.findById(messageId)
                .orElseThrow(() -> new ServiceException("Kunne ikke hente den oprettede påmindelse"));
    }
}