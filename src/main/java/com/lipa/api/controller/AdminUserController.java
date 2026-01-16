package com.lipa.api.controller;

import com.lipa.api.dto.CreateUserRequest;
import com.lipa.api.dto.CreateUserResponse;
import com.lipa.application.port.in.CreateAgentUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/backoffice/users")
public class AdminUserController {

    private final CreateAgentUserUseCase useCase;

    public AdminUserController(CreateAgentUserUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse create(@RequestBody @Valid CreateUserRequest request) {
        var res = useCase.create(new CreateAgentUserUseCase.Command(
                request.username(),
                request.password(),
                request.role()
        ));

        return new CreateUserResponse(
                res.userId(),
                res.username(),
                res.role(),
                res.status(),
                res.createdAt()
        );
    }
}
