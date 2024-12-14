package ir.dotin.loan.morabehe.core.domain.config.entity.loanrule;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.event.MorabeheLoanRuleCreatedEvent;
import ir.dotin.loan.morabehe.core.domain.config.event.MorabeheLoanRuleDisabledEvent;
import ir.dotin.loan.morabehe.core.domain.config.exception.MorabeheLoanRuleValidationException;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.entity.AggregateRoot;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static ir.dotin.platform.ddd.common.util.Validator.validate;


@SuppressWarnings("FieldMayBeFinal")
public class MorabeheLoanRule extends AggregateRoot<MorabeheLoanRuleId> {

    private LoanRule loanRule;

    public MorabeheLoanRule(MorabeheLoanRuleId morabeheLoanRuleId, LoanRule loanRule) {
        super(morabeheLoanRuleId);
        validate(v -> v.checkNotNull(loanRule, "loanRule"), MorabeheLoanRuleValidationException::new);
        this.loanRule = loanRule;
    }

    public void createLoanRule() {
        setId(MorabeheLoanRuleId.generate());
        loanRule.createLoanRule();
        registerEvent(MorabeheLoanRuleCreatedEvent.of(id()));
    }

    public void activate() {
        loanRule.activate();
    }

    public void deactivate() {
        loanRule.deactivate();
    }

    public void validateIsEnable() {
        loanRule.validateIsEnable();
    }

    public void setPreviousVersion(LoanRuleId id) {
        loanRule.setPreviousVersion(id);
    }

    public void validateIsActive() {
        loanRule.validateIsActive();
    }

    public void markAsDisabled() {
        loanRule.markAsDisabled();
        registerEvent(MorabeheLoanRuleDisabledEvent.of(id()));
    }

    public LoanRule getLoanRule() {
        return loanRule;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MorabeheLoanRule that)) {
            return false;
        }
        return new EqualsBuilder().append(id(), that.id()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id()).toHashCode();
    }
}
