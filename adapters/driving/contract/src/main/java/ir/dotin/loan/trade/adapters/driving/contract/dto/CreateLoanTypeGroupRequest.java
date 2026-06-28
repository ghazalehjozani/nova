package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateLoanTypeGroupRequest", description = "ایجاد گروه نوع تسهیلات")
public record CreateLoanTypeGroupRequest(
        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z0-9-]{1,64}$",
                message = "code must be an English string of letters, digits and hyphens (1-64 chars)")
        @Schema(description = "کد یکتای انگلیسی گروه (حروف/ارقام/خط تیره)", example = "RETAIL-001")
        String code,

        @NotBlank String title,
        @Nullable UUID parentGroupId,
        Map<String, String> metadata)
        implements BaseRequest {}
