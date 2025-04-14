package com.example.tilminsmukkekone.application;

import com.example.tilminsmukkekone.domain.classes.Event;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.domain.enums.EventType;
import com.example.tilminsmukkekone.infrastructure.repositories.EventDB;
import com.example.tilminsmukkekone.infrastructure.repositories.UserDB;
import com.example.tilminsmukkekone.infrastructure.util.ServiceException;
import com.example.tilminsmukkekone.util.Exceptions.InvalidCredentialsException;
import com.example.tilminsmukkekone.util.Exceptions.UserNotFoundException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class UserService {

    UserDB userDB;
    EventDB eventDB;

    public User authenticate(String username, String password) {
        Optional<User> userOptional = userDB.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException(username);
        }

        User user = userOptional.get();

        if (!user.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Ugyldigt password");
        }

        return user;
    }

    public User updateUserProfile(Long userId, User updatedUser) {
        Optional<User> existingUserOptional = userDB.findById(userId);
        if (!existingUserOptional.isPresent()) {
            throw new UserNotFoundException(userId);
        }

        User existingUser = existingUserOptional.get();

        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }

        if(updatedUser.getPhotoUrl() != null) {
            existingUser.setPhotoUrl(updatedUser.getPhotoUrl());
        }

        boolean updated = userDB.update(existingUser);
        if (!updated) {
            throw new ServiceException("Kunne ikke opdatere profil");
        }

        return existingUser;
    }

    public User setAnniversaryDate(Long userId, Date anniversaryDate) {
        Optional<User> existingUserOptional = userDB.findById(userId);
        if (!existingUserOptional.isPresent()) {
            throw new UserNotFoundException(userId);
        }

        User existingUser = existingUserOptional.get();

        LocalDate localAnniversaryDate = anniversaryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        existingUser.setAnniversary(localAnniversaryDate);

        boolean updated = userDB.update(existingUser);
        if (!updated) {
            throw new ServiceException("Fejl i at sætte årsdag");
        }

        return existingUser;
    }

    public long getDaysUntilAnniversary(Long userId) {
        Optional<User> existingUserOptional = userDB.findById(userId);
        if (!existingUserOptional.isPresent()) {
            throw new UserNotFoundException(userId);
        }

        User existingUser = existingUserOptional.get();
        LocalDate today = LocalDate.now();
        LocalDate anniversaryDate = existingUser.getAnniversary();

        LocalDate nextAnniversary = anniversaryDate.withYear(today.getYear());
        if (nextAnniversary.isBefore(today) || nextAnniversary.isEqual(today)) {
            nextAnniversary = nextAnniversary.plusYears(1);
        }

        return ChronoUnit.DAYS.between(today, nextAnniversary);
    }

    public List<Event> getRelationshipMilestones(Long userId) {
        Optional<User> existingUserOptional = userDB.findById(userId);
        if (!existingUserOptional.isPresent()) {
            throw new UserNotFoundException(userId);
        }

        List<Event> allUserEvents = eventDB.findByParticipantId(userId);

        List<Event> milestones = allUserEvents.stream()
                .filter(event -> event.getType() == EventType.MILESTONE ||
                        event.getType() == EventType.ANNIVERSARY)
                .sorted(Comparator.comparing(Event::getEventDate))
                .collect(Collectors.toList());

        return milestones;
    }

    public User getUserById(Long userId) {
        Optional<User> existingUserOptional = userDB.findById(userId);

        if (!existingUserOptional.isPresent()) {
            throw new UserNotFoundException(userId);
        }

        return existingUserOptional.get();
    }
}
