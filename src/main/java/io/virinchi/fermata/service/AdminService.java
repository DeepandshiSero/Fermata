package io.virinchi.fermata.service;

import io.virinchi.fermata.model.User;
import io.virinchi.fermata.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void removeUserFromPiano(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPianoEnrolled(false);
            userRepository.save(user);
        });
    }
}