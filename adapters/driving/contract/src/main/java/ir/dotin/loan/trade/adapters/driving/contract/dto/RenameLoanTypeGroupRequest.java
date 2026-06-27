package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RenameLoanTypeGroupRequest", description = "تغییر نام گروه نوع تسهیلات")
public record RenameLoanTypeGroupRequest(
        @NotNull Long version, @NotBlank String title, Map<String, String> metadata) implements BaseRequest {}
