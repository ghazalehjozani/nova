package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.contract.dto.PlanSingleInstallmentScheduleRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanSingleInstallmentScheduleCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-facilities/{facilityId}/installment-schedules/single-installment")
@Tag(name = SwaggerConfig.TAG_INSTALLMENT_SCHEDULE_PLANNING, description = "عملیات مربوط به برنامه‌ریزی جدول اقساط")
@RequiredArgsConstructor
class PlanSingleInstallmentScheduleController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "برنامه‌ریزی جدول اقساط تک‌قسطی")
    public ResponseEntity<Void> planSingleInstallmentSchedule(
            @Parameter(description = "شناسه یکتای تسهیلات", required = true) @PathVariable UUID facilityId,
            @Parameter(description = "جزئیات برنامه‌ریزی جدول اقساط تک‌قسطی", required = true) @RequestBody @Valid
                    PlanSingleInstallmentScheduleRequest request) {

        // Idempotency/correlation key comes from the filter-populated InvocationContext, never the body.
        var command = new PlanSingleInstallmentScheduleCommand(
                getIdempotencyKey(),
                request.version(),
                facilityId,
                request.totalLoanAmount(),
                request.currency(),
                request.interestRate(),
                request.gracePeriodDays());

        return responseFactory.mutated(dispatcher.dispatch(command));
    }
}
