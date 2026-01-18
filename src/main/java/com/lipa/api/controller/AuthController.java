package com.lipa.api.controller;

import com.lipa.api.dto.LoginRequest;
import com.lipa.api.dto.LoginResponse;
import com.lipa.application.port.in.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        var res = loginUseCase.login(new LoginUseCase.Command(request.username(), request.password()));

        return new LoginResponse(
                res.accessToken(),
                res.tokenType(),
                res.expiresInSeconds(),
                res.username(),
                res.role(),
                res.issuedAt(),
                res.expiresAt()
        );
    }
}
