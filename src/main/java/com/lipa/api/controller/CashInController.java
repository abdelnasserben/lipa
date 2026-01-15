package com.lipa.api.controller;

import com.lipa.api.dto.CreateCashInRequest;
import com.lipa.api.dto.CreateCashInResponse;
import com.lipa.application.port.in.CreateCashInUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class CashInController {

    private final CreateCashInUseCase createCashIn;

    public CashInController(CreateCashInUseCase createCashIn) {
        this.createCashIn = createCashIn;
    }

    @PostMapping("/cash-ins")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCashInResponse create(@RequestBody @Valid CreateCashInRequest request) {
        var result = createCashIn.create(new CreateCashInUseCase.Command(
                request.clientAccountId(),
                request.amount(),
                request.currency(),
                request.idempotencyKey(),
                request.description(),
                request.technicalAccountId()
        ));
        return new CreateCashInResponse(result.transactionId(), result.status());
    }
}
