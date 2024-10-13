package hu.therealuhlarzoltan.expensables.cloud.authserver.controllers;

import hu.therealuhlarzoltan.expensables.cloud.authserver.config.JwtTokenProvider;
import hu.therealuhlarzoltan.expensables.cloud.authserver.models.UserEntity;
import hu.therealuhlarzoltan.expensables.cloud.authserver.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class AuthControllerImpl implements AuthController {
    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @Override
    public UserEntity registerUser(@Valid UserEntity user) {
        return authService.registerUser(user);
    }

    @Override
    public String loginUser(UserEntity user) {
        Authentication authentication = authService.authenticateUser(user.getUsername(), user.getPassword());
        UserEntity authenticatedUser = authService.getUserByUsername(user.getUsername());
        return tokenProvider.generateToken(authentication, authenticatedUser.getId());
    }
}
