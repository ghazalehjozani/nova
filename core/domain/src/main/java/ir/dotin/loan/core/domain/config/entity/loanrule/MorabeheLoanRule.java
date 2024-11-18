package ir.dotin.loan.core.domain.config.entity.loanrule;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleId;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanTypeId;
import ir.dotin.loan.core.domain.config.entity.exception.MorabeheLoanRuleException;
import ir.dotin.loan.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.core.domain.config.event.MorabeheLoanRuleCreatedEvent;
import ir.dotin.loan.core.domain.config.event.MorabeheLoanRuleDisabledEvent;
import ir.dotin.loan.core.domain.config.event.MorabeheLoanTypeDisabledEvent;
import ir.dotin.loan.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.entity.AggregateRoot;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static ir.dotin.platform.ddd.common.util.Validator.validate;


@SuppressWarnings("FieldMayBeFinal")
public class MorabeheLoanRule extends AggregateRoot<MorabeheLoanRuleId> {

    private LoanRule loanRule;

    public MorabeheLoanRule(MorabeheLoanRuleId morabeheLoanRuleId, LoanRule.LoanRuleBuilder loanRuleBuilder) {
        super(morabeheLoanRuleId);
        validate(v -> v.checkNotNull(loanRuleBuilder, "loanRuleBuilder"),
                MorabeheLoanRuleException::new);
        this.loanRule = loanRuleBuilder.validateAndBuild();
    }

    public void createLoanRule() {
        setId(MorabeheLoanRuleId.generate());
        loanRule.createLoanRule();
        registerEvent(MorabeheLoanRuleCreatedEvent.of(getId()));
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
        registerEvent(MorabeheLoanRuleDisabledEvent.of(getId()));
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
        return new EqualsBuilder().append(getId(), that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
    }
}
