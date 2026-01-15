package com.lipa.api.controller;

import com.lipa.api.dto.LoginRequest;
import com.lipa.api.dto.LoginResponse;
import com.lipa.infrastructure.security.AuthService;
import com.lipa.application.port.out.TimeProviderPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final TimeProviderPort time;

    public AuthController(AuthService authService, TimeProviderPort time) {
        this.authService = authService;
        this.time = time;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        var now = time.now();
        var result = authService.login(request.username(), request.password(), now);

        return new LoginResponse(
                result.accessToken(),
                "Bearer",
                result.expiresInSeconds(),
                result.username(),
                result.role(),
                result.issuedAt(),
                result.expiresAt()
        );
    }
}
