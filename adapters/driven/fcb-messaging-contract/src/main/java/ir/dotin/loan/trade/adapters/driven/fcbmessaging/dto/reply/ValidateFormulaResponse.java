package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class ValidateFormulaResponse extends FcbBaseResponse {

    private boolean valid;

    private @Nullable List<String> violations;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public @Nullable List<String> getViolations() {
        return violations;
    }

    public void setViolations(@Nullable List<String> violations) {
        this.violations = violations;
    }
}
