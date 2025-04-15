package com.example.tilminsmukkekone.application;

import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.infrastructure.repositories.UserDB;
import com.example.tilminsmukkekone.infrastructure.util.ServiceException;
import com.example.tilminsmukkekone.util.Exceptions.InvalidCredentialsException;
import com.example.tilminsmukkekone.util.Exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class UserService {

    private final UserDB userDB;

    @Autowired
    public UserService(UserDB userDB) {
        this.userDB = userDB;
    }

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

    public User getUserById(Long userId) {
        Optional<User> existingUserOptional = userDB.findById(userId);

        if (!existingUserOptional.isPresent()) {
            throw new UserNotFoundException(userId);
        }

        return existingUserOptional.get();
    }
}