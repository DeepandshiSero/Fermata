package io.virinchi.fermata.service;

import io.virinchi.fermata.model.User;
import io.virinchi.fermata.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class PianoService {

    private final UserRepository userRepository;

    public PianoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void enrollUserInPiano(User user) {
        user.setPianoEnrolled(true);
        userRepository.save(user);
    }
}