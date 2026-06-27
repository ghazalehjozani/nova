package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AssignLoanTypeToGroupRequest", description = "تخصیص نوع تسهیلات به گروه")
public record AssignLoanTypeToGroupRequest(
        @NotNull Long version, @NotNull UUID groupId, Map<String, String> metadata) implements BaseRequest {}
