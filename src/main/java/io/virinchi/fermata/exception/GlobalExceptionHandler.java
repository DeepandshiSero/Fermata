package io.virinchi.fermata.exception;

import io.virinchi.fermata.dto.ErrorResponse;
import io.virinchi.fermata.dto.UserLoginDto;
import io.virinchi.fermata.dto.UserSignupDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        // Missing static files (favicon.ico, images, etc.) - just return a plain 404, no page rendering
        log.debug("Static resource not found: {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Object handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("Login failed - user not found: {}", ex.getMessage());
        if (isJsonRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        ModelAndView mav = new ModelAndView("LogIn");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("user", new UserLoginDto());
        return mav;
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public Object handleIncorrectPassword(IncorrectPasswordException ex, HttpServletRequest request) {
        log.warn("Login failed - incorrect password: {}", ex.getMessage());
        if (isJsonRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized",
                    ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        ModelAndView mav = new ModelAndView("LogIn");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("user", new UserLoginDto());
        return mav;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Object handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("User registration conflict: {}", ex.getMessage());
        if (isJsonRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.CONFLICT.value(),
                    "Conflict",
                    ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
        ModelAndView mav = new ModelAndView("SignIn");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("user", new UserSignupDto());
        return mav;
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public Object handlePasswordMismatch(PasswordMismatchException ex, HttpServletRequest request) {
        log.warn("Password mismatch: {}", ex.getMessage());
        if (isJsonRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        ModelAndView mav = new ModelAndView("SignIn");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("user", new UserSignupDto());
        return mav;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Validation failed: {}", errors);

        if (isJsonRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation Error",
                    "Invalid input parameters",
                    errors
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        ModelAndView mav = new ModelAndView("SignIn");
        mav.addObject("fieldErrors", errors);
        mav.addObject("errorMessage", "Please correct the errors in the form.");
        mav.addObject("user", new UserSignupDto());
        return mav;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage());
        String message = "Database constraint violation. The username might already be in use.";
        if (isJsonRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.CONFLICT.value(),
                    "Conflict",
                    message
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
        ModelAndView mav = new ModelAndView("SignIn");
        mav.addObject("errorMessage", message);
        mav.addObject("user", new UserSignupDto());
        return mav;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        if (isJsonRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        ModelAndView mav = new ModelAndView("SignIn");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("user", new UserSignupDto());
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public Object handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception caught: ", ex);
        String message = "An unexpected error occurred. Please try again later.";
        if (isJsonRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    message
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
        ModelAndView mav = new ModelAndView("SignIn");
        mav.addObject("errorMessage", message);
        mav.addObject("user", new UserSignupDto());
        return mav;
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        String accept = request.getHeader("Accept");
        return (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE))
                || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));
    }
}