package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.contract.dto.AssignLoanTypeToGroupRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AssignLoanTypeToGroupCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RemoveLoanTypeFromGroupCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-types")
@Tag(name = SwaggerConfig.TAG_LOAN_TYPE_GROUP_COMMANDS, description = "مدیریت عضویت نوع تسهیلات در گروه")
@RequiredArgsConstructor
class LoanTypeGroupMembershipController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final CommandResponseFactory responseFactory;

    @PutMapping(value = "/{loanTypeId}/group", version = "1+")
    @Operation(summary = "تخصیص نوع تسهیلات به گروه")
    public ResponseEntity<Void> assign(
            @PathVariable UUID loanTypeId, @RequestBody @Valid AssignLoanTypeToGroupRequest request) {
        var command = AssignLoanTypeToGroupCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .loanTypeId(loanTypeId)
                .groupId(request.groupId())
                .build();
        return responseFactory.mutated(dispatcher.dispatch(command));
    }

    @DeleteMapping(value = "/{loanTypeId}/group", version = "1+")
    @Operation(summary = "حذف نوع تسهیلات از گروه")
    public ResponseEntity<Void> unassign(
            @PathVariable UUID loanTypeId,
            @Parameter(description = "نسخه عملیات (قفل خوش‌بینانه)", example = "1", required = true) @RequestParam
                    Long version) {
        var command = RemoveLoanTypeFromGroupCommand.builder()
                .uid(getIdempotencyKey())
                .version(version)
                .loanTypeId(loanTypeId)
                .build();
        return responseFactory.mutated(dispatcher.dispatch(command));
    }
}
