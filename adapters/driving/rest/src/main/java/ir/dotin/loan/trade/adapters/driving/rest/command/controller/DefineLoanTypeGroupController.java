package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.controller.CommandResponseFactory;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CreateLoanTypeGroupRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.MoveLoanTypeGroupRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.RenameLoanTypeGroupRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CreateLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.MoveLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RenameLoanTypeGroupCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-type-groups")
@Tag(name = SwaggerConfig.TAG_LOAN_TYPE_GROUP_COMMANDS, description = "مدیریت گروه نوع تسهیلات")
@RequiredArgsConstructor
class DefineLoanTypeGroupController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "ایجاد گروه نوع تسهیلات")
    public ResponseEntity<Void> create(@RequestBody @Valid CreateLoanTypeGroupRequest request) {
        var command = CreateLoanTypeGroupCommand.builder()
                .uid(getIdempotencyKey())
                .version(null)
                .title(request.title())
                .parentGroupId(request.parentGroupId())
                .build();
        var result = dispatcher.dispatch(command);
        return responseFactory.created(result, "loan-type-groups");
    }

    @PatchMapping(value = "/{groupId}", version = "1+")
    @Operation(summary = "تغییر نام گروه")
    public ResponseEntity<Void> rename(
            @PathVariable UUID groupId, @RequestBody @Valid RenameLoanTypeGroupRequest request) {
        var command = RenameLoanTypeGroupCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .groupId(groupId)
                .title(request.title())
                .build();
        return responseFactory.mutated(dispatcher.dispatch(command));
    }

    @PatchMapping(value = "/{groupId}/parent", version = "1+")
    @Operation(summary = "جابجایی گروه در درخت")
    public ResponseEntity<Void> move(@PathVariable UUID groupId, @RequestBody @Valid MoveLoanTypeGroupRequest request) {
        var command = MoveLoanTypeGroupCommand.builder()
                .uid(getIdempotencyKey())
                .version(request.version())
                .groupId(groupId)
                .newParentGroupId(request.newParentGroupId())
                .build();
        return responseFactory.mutated(dispatcher.dispatch(command));
    }
}
