package io.virinchi.fermata.service;

import io.virinchi.fermata.dto.UserLoginDto;
import io.virinchi.fermata.dto.UserSignupDto;
import io.virinchi.fermata.model.User;

public interface UserService {

    User registerUser(UserSignupDto signupDto);

    boolean isUsernameTaken(String username);

    User authenticate(String username, String password);

    User loginUser(UserLoginDto loginDto);
}
