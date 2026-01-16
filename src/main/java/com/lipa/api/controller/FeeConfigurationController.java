package com.lipa.api.controller;

import com.lipa.api.dto.FeeConfigurationRequest;
import com.lipa.api.dto.FeeConfigurationResponse;
import com.lipa.application.port.in.GetCurrentFeeConfigurationUseCase;
import com.lipa.application.port.in.UpdateFeeConfigurationUseCase;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/backoffice/fees")
public class FeeConfigurationController {

    private final GetCurrentFeeConfigurationUseCase getUseCase;
    private final UpdateFeeConfigurationUseCase updateUseCase;

    public FeeConfigurationController(GetCurrentFeeConfigurationUseCase getUseCase,
                                      UpdateFeeConfigurationUseCase updateUseCase) {
        this.getUseCase = getUseCase;
        this.updateUseCase = updateUseCase;
    }

    @GetMapping("/current")
    public FeeConfigurationResponse get() {
        var res = getUseCase.get();
        return new FeeConfigurationResponse(
                res.percentage(),
                res.minAmount(),
                res.maxAmount(),
                res.currency(),
                res.updatedAt()
        );
    }

    @PutMapping("/current")
    public FeeConfigurationResponse update(@RequestBody FeeConfigurationRequest request) {
        var res = updateUseCase.update(new UpdateFeeConfigurationUseCase.Command(
                request.percentage(),
                request.minAmount(),
                request.maxAmount(),
                request.currency()
        ));

        return new FeeConfigurationResponse(
                res.percentage(),
                res.minAmount(),
                res.maxAmount(),
                res.currency(),
                res.updatedAt()
        );
    }
}
