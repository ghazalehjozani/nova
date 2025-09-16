package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.platform.commons.domain.event.DomainEvent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response for Commands")
public class BaseResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<DomainEvent<?, ?>> events;
}
