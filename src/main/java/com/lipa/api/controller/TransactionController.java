package com.lipa.api.controller;

import com.lipa.api.dto.CreatePaymentRequest;
import com.lipa.api.dto.CreatePaymentResponse;
import com.lipa.application.port.in.CreatePaymentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final CreatePaymentUseCase createPayment;

    public TransactionController(CreatePaymentUseCase createPayment) {
        this.createPayment = createPayment;
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePaymentResponse createPayment(@RequestBody @Valid CreatePaymentRequest request) {
        var result = createPayment.create(new CreatePaymentUseCase.Command(
                request.cardUid(),
                request.pin(),
                request.merchantAccountId(),
                request.amount(),
                request.currency(),
                request.idempotencyKey(),
                request.description()
        ));
        return new CreatePaymentResponse(result.transactionId(), result.status());
    }
}
