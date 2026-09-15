package io.virinchi.fermata.controller;

import io.virinchi.fermata.dto.ApiResponse;
import io.virinchi.fermata.dto.UserLoginDto;
import io.virinchi.fermata.dto.UserResponseDto;
import io.virinchi.fermata.dto.UserSignupDto;
import io.virinchi.fermata.exception.IncorrectPasswordException;
import io.virinchi.fermata.exception.PasswordMismatchException;
import io.virinchi.fermata.exception.UserAlreadyExistsException;
import io.virinchi.fermata.exception.UserNotFoundException;
import io.virinchi.fermata.model.User;
import io.virinchi.fermata.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Display the login page.
     */
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserLoginDto());
        }
        return "LogIn";
    }

    /**
     * Handle Spring MVC form submission for user login.
     */
    @PostMapping("/login")
    public String processLoginForm(
            @Valid @ModelAttribute("user") UserLoginDto loginDto,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "LogIn";
        }

        try {
            User user = userService.authenticate(loginDto.getUsername(), loginDto.getPassword());
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedInUser", user);
            session.setAttribute("username", user.getUsername());
            return "redirect:/Home.html";
        } catch (UserNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            bindingResult.rejectValue("username", "notFound", ex.getMessage());
            return "LogIn";
        } catch (IncorrectPasswordException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            bindingResult.rejectValue("password", "incorrect", ex.getMessage());
            return "LogIn";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Login failed: " + ex.getMessage());
            return "LogIn";
        }
    }

    /**
     * Handle REST API JSON user login requests.
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse<UserResponseDto>> processLoginJson(
            @Valid @RequestBody UserLoginDto loginDto,
            HttpServletRequest request) {

        User user = userService.authenticate(loginDto.getUsername(), loginDto.getPassword());
        HttpSession session = request.getSession(true);
        session.setAttribute("loggedInUser", user);
        session.setAttribute("username", user.getUsername());

        UserResponseDto responseData = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
        return ResponseEntity.ok(ApiResponse.success("Login successful", responseData));
    }

    /**
     * Handle user logout.
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

    /**
     * Display the signup / registration page.
     */
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserSignupDto());
        }
        return "SignIn";
    }

    /**
     * Handle Spring MVC form submission for user registration.
     */
    @PostMapping("/signup")
    public String processSignupForm(
            @Valid @ModelAttribute("user") UserSignupDto signupDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "SignIn";
        }

        try {
            userService.registerUser(signupDto);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Please sign in.");
            return "redirect:/login";
        } catch (UserAlreadyExistsException ex) {
            bindingResult.rejectValue("username", "duplicate", ex.getMessage());
            return "SignIn";
        } catch (PasswordMismatchException ex) {
            bindingResult.rejectValue("confirmPassword", "mismatch", ex.getMessage());
            return "SignIn";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Registration failed: " + ex.getMessage());
            return "SignIn";
        }
    }

    /**
     * Handle REST API JSON user registration requests.
     */
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse<UserResponseDto>> processSignupJson(
            @Valid @RequestBody UserSignupDto signupDto) {

        User user = userService.registerUser(signupDto);
        UserResponseDto responseData = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", responseData));
    }
}
