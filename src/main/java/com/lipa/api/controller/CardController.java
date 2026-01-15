package com.lipa.api.controller;

import com.lipa.api.dto.*;
import com.lipa.application.port.in.EnrollCardUseCase;
import com.lipa.application.port.in.SetPinUseCase;
import com.lipa.application.port.in.VerifyPinUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final EnrollCardUseCase enrollCard;
    private final SetPinUseCase setPin;
    private final VerifyPinUseCase verifyPin;

    public CardController(EnrollCardUseCase enrollCard,
                          SetPinUseCase setPin,
                          VerifyPinUseCase verifyPin) {
        this.enrollCard = enrollCard;
        this.setPin = setPin;
        this.verifyPin = verifyPin;
    }

    @PostMapping("/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollCardResponse enroll(@RequestBody @Valid EnrollCardRequest request) {
        var result = enrollCard.enroll(new EnrollCardUseCase.Command(
                request.cardUid(),
                request.displayName(),
                request.phone()
        ));
        return new EnrollCardResponse(result.accountId(), result.cardId(), result.cardUid());
    }

    @PostMapping("/{uid}/pin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPin(@PathVariable("uid") String uid, @RequestBody @Valid SetPinRequest request) {
        setPin.setPin(new SetPinUseCase.Command(uid, request.pin(), request.reason()));
    }

    @PostMapping("/{uid}/pin/verify")
    public VerifyPinResponse verify(@PathVariable("uid") String uid, @RequestBody @Valid VerifyPinRequest request) {
        var result = verifyPin.verify(new VerifyPinUseCase.Command(uid, request.pin()));
        return new VerifyPinResponse(result.success(), result.cardBlocked());
    }
}
