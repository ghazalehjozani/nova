package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SamatViolationDto;

public final class ValidateSamatKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable List<SamatViolationDto> violations;

    public @Nullable List<SamatViolationDto> getViolations() {
        return violations;
    }

    public void setViolations(@Nullable List<SamatViolationDto> violations) {
        this.violations = violations;
    }
}
