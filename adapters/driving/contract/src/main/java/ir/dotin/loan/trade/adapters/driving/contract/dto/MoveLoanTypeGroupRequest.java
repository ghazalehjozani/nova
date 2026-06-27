package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MoveLoanTypeGroupRequest", description = "جابجایی گروه در درخت")
public record MoveLoanTypeGroupRequest(
        @NotNull Long version, @Nullable UUID newParentGroupId, Map<String, String> metadata) implements BaseRequest {}
