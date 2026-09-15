package io.virinchi.fermata.service;

import io.virinchi.fermata.model.User;
import io.virinchi.fermata.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void removeUserFromPiano(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPianoEnrolled(false);
            userRepository.save(user);
        });
    }
}