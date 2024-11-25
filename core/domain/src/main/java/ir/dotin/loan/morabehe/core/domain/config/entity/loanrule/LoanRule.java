package ir.dotin.loan.morabehe.core.domain.config.entity.loanrule;

import ir.dotin.loan.baseloan.domain.config.entity.loanrule.BaseLoanRule;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.exception.MorabeheLoanRuleValidationException;
import ir.dotin.platform.ddd.common.exception.ValidationError;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import ir.dotin.platform.ddd.common.util.Validator;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


@SuppressWarnings("FieldMayBeFinal")
public class LoanRule extends BaseLoanRule {

    LoanRule(LoanRuleBuilder builder) {
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

        public LoanRuleBuilder(FeatureConfig featureConfig) {
            super(featureConfig);
        }

        public LoanRuleBuilder(FeatureConfig featureConfig, LoanRule other) {
            super(featureConfig, other);
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
            List<ValidationError> errors = super.validateBaseInvariants();
            Validator.validate(v -> v.appendErrors(errors), MorabeheLoanRuleValidationException::new);
        }

    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LoanRule that)) {
            return false;
        }
        return new EqualsBuilder().append(getId(), that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
    }

}
