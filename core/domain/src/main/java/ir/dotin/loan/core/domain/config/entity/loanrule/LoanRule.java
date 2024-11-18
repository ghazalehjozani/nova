package ir.dotin.loan.core.domain.config.entity.loanrule;

import ir.dotin.loan.baseloan.domain.config.entity.loanrule.BaseLoanRule;
import ir.dotin.loan.baseloan.domain.config.exception.LoanRuleException;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleId;
import ir.dotin.loan.core.domain.config.entity.loantype.LoanType;
import ir.dotin.platform.ddd.common.exception.DomainError;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import ir.dotin.platform.ddd.common.util.Validator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;


@SuppressWarnings("FieldMayBeFinal")
public class LoanRule extends BaseLoanRule {

    LoanRule(BaseLoanRuleBuilder<?> builder) {
        super(builder);
    }

    @Override
    protected void createLoanRule() {
        super.createLoanRule();
    }


    @Override
    protected void activate() {
        super.activate();
    }


    @Override
    protected void deactivate() {
        super.deactivate();
    }

    @Override
    protected void markAsDisabled() {
        super.markAsDisabled();
    }

    @Override
    protected void validateIsEnable() {
        super.validateIsEnable();
    }

    @Override
    protected void setPreviousVersion(LoanRuleId id) {
        super.setPreviousVersion(id);
    }

    @Override
    protected void validateIsActive() {
        super.validateIsActive();
    }


    public static class LoanRuleBuilder extends BaseLoanRuleBuilder<LoanRuleBuilder> {

        protected LoanRuleBuilder(FeatureConfig featureConfig) {
            super(featureConfig);
        }

        @Override
        public LoanRuleBuilder self() {
            return this;
        }

        @Override
        public LoanRule validateAndBuild() {
            validateInvariants();
            return new LoanRule(this);
        }

        private void validateInvariants() {
            List<DomainError> errors = super.validateBaseInvariants();
            Validator.validate(v -> v.appendErrors(errors), LoanRuleException::new);
        }

    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LoanType that)) {
            return false;
        }
        return new EqualsBuilder().append(getId(), that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
    }

}
