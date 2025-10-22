package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "IssueFacilityContractRequest", description = "عملیات صدور قرارداد")
public record IssueFacilityContractRequest(
        @Schema(
                        description = "شناسه عملیات",
                        example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "شناسه عملیات الزامی است.")
                @JsonProperty("uid")
                UUID uid,
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "نسخه عملیات الزامی است.")
                @JsonProperty("version")
                Integer version) {}
