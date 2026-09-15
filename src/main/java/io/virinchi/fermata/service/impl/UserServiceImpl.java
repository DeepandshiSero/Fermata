package io.virinchi.fermata.service.impl;

import io.virinchi.fermata.dto.UserLoginDto;
import io.virinchi.fermata.dto.UserSignupDto;
import io.virinchi.fermata.exception.IncorrectPasswordException;
import io.virinchi.fermata.exception.PasswordMismatchException;
import io.virinchi.fermata.exception.UserAlreadyExistsException;
import io.virinchi.fermata.exception.UserNotFoundException;
import io.virinchi.fermata.model.User;
import io.virinchi.fermata.repository.UserRepository;
import io.virinchi.fermata.service.EmailService;
import io.virinchi.fermata.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public User registerUser(UserSignupDto signupDto) {
        if (signupDto == null) {
            throw new IllegalArgumentException("Signup data cannot be null");
        }

        String username = signupDto.getUsername() != null ? signupDto.getUsername().trim() : "";
        String password = signupDto.getPassword();
        String confirmPassword = signupDto.getConfirmPassword();

        // Validate password confirmation match
        if (password == null || !password.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match. Please re-enter your password.");
        }

        // Validate username uniqueness
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username '" + username + "' is already taken. Please choose another.");
        }

        // Validate terms acceptance
        if (!signupDto.isTermsAccepted()) {
            throw new IllegalArgumentException("You must agree to the Terms & Conditions.");
        }

        // Encrypt credentials before saving to database
        String encodedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setUsername(username);
        user.setFullName(signupDto.getName() != null ? signupDto.getName().trim() : "");
        user.setEmail(signupDto.getEmail() != null ? signupDto.getEmail().trim() : "");
        user.setPassword(encodedPassword);
        user.setTermsAccepted(signupDto.isTermsAccepted());
        user.setRole("ROLE_USER");

        User savedUser = userRepository.save(user);

        try {
            emailService.sendRegistrationConfirmation(savedUser.getEmail(), savedUser.getFullName());
        } catch (Exception ex) {
            // Don't fail registration if the email fails to send
            System.err.println("Failed to send registration email: " + ex.getMessage());
        }

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameTaken(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return userRepository.existsByUsername(username.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public User authenticate(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new UserNotFoundException("User does not exist");
        }

        String trimmedUsername = username.trim();
        User user = userRepository.findByUsername(trimmedUsername)
                .or(() -> userRepository.findByUsernameIgnoreCase(trimmedUsername))
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        boolean passwordMatches = false;
        if (password != null) {
            try {
                passwordMatches = passwordEncoder.matches(password, user.getPassword());
            } catch (Exception ignored) {
            }
            if (!passwordMatches && password.equals(user.getPassword())) {
                passwordMatches = true;
            }
        }

        if (!passwordMatches) {
            throw new IncorrectPasswordException("Password is incorrect");
        }

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User loginUser(UserLoginDto loginDto) {
        if (loginDto == null) {
            throw new IllegalArgumentException("Login data cannot be null");
        }
        return authenticate(loginDto.getUsername(), loginDto.getPassword());
    }
}