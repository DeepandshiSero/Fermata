package io.virinchi.fermata.controller;

import io.virinchi.fermata.dto.UserSignupDto;
import io.virinchi.fermata.exception.GlobalExceptionHandler;
import io.virinchi.fermata.exception.IncorrectPasswordException;
import io.virinchi.fermata.exception.PasswordMismatchException;
import io.virinchi.fermata.exception.UserAlreadyExistsException;
import io.virinchi.fermata.exception.UserNotFoundException;
import io.virinchi.fermata.model.User;
import io.virinchi.fermata.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testShowSignupForm() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("SignIn"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void testProcessSignupForm_Success() throws Exception {
        User user = new User("alice", "Alice Smith", "hashed123", true);
        user.setId(1L);
        when(userService.registerUser(any(UserSignupDto.class))).thenReturn(user);

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "alice")
                        .param("name", "Alice Smith")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("termsAccepted", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testProcessSignupForm_ValidationFailure() throws Exception {
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "") // Blank username triggers validation
                        .param("name", "Alice")
                        .param("password", "123") // Too short
                        .param("confirmPassword", "123")
                        .param("termsAccepted", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("SignIn"))
                .andExpect(model().hasErrors());
    }

    @Test
    void testProcessSignupForm_UserAlreadyExists() throws Exception {
        when(userService.registerUser(any(UserSignupDto.class)))
                .thenThrow(new UserAlreadyExistsException("Username 'alice' is already taken"));

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "alice")
                        .param("name", "Alice Smith")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("termsAccepted", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("SignIn"))
                .andExpect(model().attributeHasFieldErrors("user", "username"));
    }

    @Test
    void testProcessSignupJson_Success() throws Exception {
        User user = new User("bob123", "Bob Jones", "hashed123", true);
        user.setId(2L);
        when(userService.registerUser(any(UserSignupDto.class))).thenReturn(user);

        String jsonPayload = """
                {
                    "username": "bob123",
                    "name": "Bob Jones",
                    "password": "password123",
                    "confirmPassword": "password123",
                    "termsAccepted": true
                }
                """;

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("bob123"));
    }

    @Test
    void testProcessSignupJson_DuplicateUserReturns409() throws Exception {
        when(userService.registerUser(any(UserSignupDto.class)))
                .thenThrow(new UserAlreadyExistsException("Username 'bob123' is already taken"));

        String jsonPayload = """
                {
                    "username": "bob123",
                    "name": "Bob Jones",
                    "password": "password123",
                    "confirmPassword": "password123",
                    "termsAccepted": true
                }
                """;

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username 'bob123' is already taken"));
    }

    @Test
    void testProcessSignupJson_PasswordMismatchReturns400() throws Exception {
        when(userService.registerUser(any(UserSignupDto.class)))
                .thenThrow(new PasswordMismatchException("Passwords do not match"));

        String jsonPayload = """
                {
                    "username": "bob123",
                    "name": "Bob Jones",
                    "password": "password123",
                    "confirmPassword": "mismatch123",
                    "termsAccepted": true
                }
                """;

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Passwords do not match"));
    }

    @Test
    void testShowLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("LogIn"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void testProcessLoginForm_Success() throws Exception {
        User user = new User("valid_user", "Valid User", "hashedSecret", true);
        user.setId(10L);
        when(userService.authenticate("valid_user", "correct_pass")).thenReturn(user);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "valid_user")
                        .param("password", "correct_pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/Home.html"));
    }

    @Test
    void testProcessLoginForm_UserDoesNotExist() throws Exception {
        when(userService.authenticate("ghost_user", "any_pass"))
                .thenThrow(new UserNotFoundException("User does not exist"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "ghost_user")
                        .param("password", "any_pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("LogIn"))
                .andExpect(model().attribute("errorMessage", "User does not exist"))
                .andExpect(model().attributeHasFieldErrors("user", "username"));
    }

    @Test
    void testProcessLoginForm_PasswordIsIncorrect() throws Exception {
        when(userService.authenticate("valid_user", "wrong_pass"))
                .thenThrow(new IncorrectPasswordException("Password is incorrect"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "valid_user")
                        .param("password", "wrong_pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("LogIn"))
                .andExpect(model().attribute("errorMessage", "Password is incorrect"))
                .andExpect(model().attributeHasFieldErrors("user", "password"));
    }

    @Test
    void testProcessLoginJson_Success() throws Exception {
        User user = new User("valid_user", "Valid User", "hashedSecret", true);
        user.setId(10L);
        when(userService.authenticate("valid_user", "correct_pass")).thenReturn(user);

        String jsonPayload = """
                {
                    "username": "valid_user",
                    "password": "correct_pass"
                }
                """;

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.username").value("valid_user"));
    }

    @Test
    void testProcessLoginJson_UserDoesNotExist() throws Exception {
        when(userService.authenticate("ghost_user", "any_pass"))
                .thenThrow(new UserNotFoundException("User does not exist"));

        String jsonPayload = """
                {
                    "username": "ghost_user",
                    "password": "any_pass"
                }
                """;

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User does not exist"));
    }

    @Test
    void testProcessLoginJson_PasswordIsIncorrect() throws Exception {
        when(userService.authenticate("valid_user", "wrong_pass"))
                .thenThrow(new IncorrectPasswordException("Password is incorrect"));

        String jsonPayload = """
                {
                    "username": "valid_user",
                    "password": "wrong_pass"
                }
                """;

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Password is incorrect"));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
