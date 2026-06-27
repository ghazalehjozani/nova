package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateLoanTypeGroupRequest", description = "ایجاد گروه نوع تسهیلات")
public record CreateLoanTypeGroupRequest(
        @NotBlank String title, @Nullable UUID parentGroupId, Map<String, String> metadata) implements BaseRequest {}
