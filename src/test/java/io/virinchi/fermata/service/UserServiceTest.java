package io.virinchi.fermata.service;

import io.virinchi.fermata.dto.UserSignupDto;
import io.virinchi.fermata.exception.PasswordMismatchException;
import io.virinchi.fermata.exception.UserAlreadyExistsException;
import io.virinchi.fermata.model.User;
import io.virinchi.fermata.repository.UserRepository;
import io.virinchi.fermata.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserSignupDto validSignupDto;

    @BeforeEach
    void setUp() {
        validSignupDto = new UserSignupDto();
        validSignupDto.setUsername("john_doe");
        validSignupDto.setName("John Doe");
        validSignupDto.setPassword("secret123");
        validSignupDto.setConfirmPassword("secret123");
        validSignupDto.setTermsAccepted(true);
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashedSecret123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = userService.registerUser(validSignupDto);

        assertNotNull(registeredUser);
        assertEquals("john_doe", registeredUser.getUsername());
        assertEquals("John Doe", registeredUser.getFullName());
        assertEquals("hashedSecret123", registeredUser.getPassword());
        assertEquals("ROLE_USER", registeredUser.getRole());
        assertTrue(registeredUser.isTermsAccepted());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("hashedSecret123", userCaptor.getValue().getPassword());
    }

    @Test
    void registerUser_ThrowsPasswordMismatchException() {
        validSignupDto.setConfirmPassword("differentPassword");

        PasswordMismatchException exception = assertThrows(
                PasswordMismatchException.class,
                () -> userService.registerUser(validSignupDto)
        );

        assertTrue(exception.getMessage().contains("Passwords do not match"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsUserAlreadyExistsException() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(validSignupDto)
        );

        assertTrue(exception.getMessage().contains("already taken"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsExceptionWhenTermsNotAccepted() {
        validSignupDto.setTermsAccepted(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(validSignupDto)
        );

        assertTrue(exception.getMessage().contains("Terms & Conditions"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_Success() {
        User user = new User("john_doe", "John Doe", "hashedSecret123", true);
        when(userRepository.findByUsername("john_doe")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashedSecret123")).thenReturn(true);

        User authenticatedUser = userService.authenticate("john_doe", "secret123");

        assertNotNull(authenticatedUser);
        assertEquals("john_doe", authenticatedUser.getUsername());
    }

    @Test
    void authenticate_UserDoesNotExist() {
        when(userRepository.findByUsername("unknown_user")).thenReturn(java.util.Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("unknown_user")).thenReturn(java.util.Optional.empty());

        io.virinchi.fermata.exception.UserNotFoundException exception = assertThrows(
                io.virinchi.fermata.exception.UserNotFoundException.class,
                () -> userService.authenticate("unknown_user", "secret123")
        );

        assertEquals("User does not exist", exception.getMessage());
    }

    @Test
    void authenticate_PasswordIsIncorrect() {
        User user = new User("john_doe", "John Doe", "hashedSecret123", true);
        when(userRepository.findByUsername("john_doe")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedSecret123")).thenReturn(false);

        io.virinchi.fermata.exception.IncorrectPasswordException exception = assertThrows(
                io.virinchi.fermata.exception.IncorrectPasswordException.class,
                () -> userService.authenticate("john_doe", "wrongpassword")
        );

        assertEquals("Password is incorrect", exception.getMessage());
    }

    @Test
    void loginUser_Success() {
        User user = new User("john_doe", "John Doe", "hashedSecret123", true);
        when(userRepository.findByUsername("john_doe")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashedSecret123")).thenReturn(true);

        io.virinchi.fermata.dto.UserLoginDto loginDto = new io.virinchi.fermata.dto.UserLoginDto("john_doe", "secret123");
        User loggedInUser = userService.loginUser(loginDto);

        assertNotNull(loggedInUser);
        assertEquals("john_doe", loggedInUser.getUsername());
    }

    @Test
    void loginUser_ThrowsExceptionWhenNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.loginUser(null));
    }
}
