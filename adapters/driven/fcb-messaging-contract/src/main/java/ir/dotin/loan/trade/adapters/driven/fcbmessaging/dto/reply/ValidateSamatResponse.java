package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SamatViolationDto;

public final class ValidateSamatResponse extends FcbBaseResponse {

    private @Nullable List<SamatViolationDto> violations;

    public @Nullable List<SamatViolationDto> getViolations() {
        return violations;
    }

    public void setViolations(@Nullable List<SamatViolationDto> violations) {
        this.violations = violations;
    }
}
