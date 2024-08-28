package hu.therealuhlarzoltan.expensables.cloud.authserver.controllers;

import hu.therealuhlarzoltan.expensables.cloud.authserver.models.UserEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthController {
    @PostMapping("/api/auth/register")
    UserEntity registerUser(@Valid @RequestBody UserEntity user);

    @PostMapping("/api/auth/login")
    String loginUser(@RequestBody UserEntity user);
}
