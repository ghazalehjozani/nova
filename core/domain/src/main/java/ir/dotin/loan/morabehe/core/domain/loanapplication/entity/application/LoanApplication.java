package ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application;

import ir.dotin.loan.baseloan.domain.loanapplication.entity.application.BaseLoanApplication;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.CollateralSerial;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;
import ir.dotin.loan.morabehe.core.domain.loanapplication.exception.MorabeheLoanApplicationValidationException;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import ir.dotin.platform.ddd.common.util.Validator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@SuppressWarnings("FieldMayBeFinal")
public class LoanApplication extends BaseLoanApplication {


    LoanApplication(LoanApplicationBuilder builder) {
        super(builder);
    }


    @Override
    protected void request(ApplicationNumber applicationNumber) {
        super.request(applicationNumber);
    }

    @Override
    protected void approve(SanctionSerial sanctionSerial) {
        super.approve(sanctionSerial);
    }

    @Override
    protected void addCollateral(CollateralSerial collateralSerial) {
        super.addCollateral(collateralSerial);
    }

    @Override
    protected void issueContract() {
        super.issueContract();
    }

    @Override
    protected void disburse() {
        super.disburse();
    }

    @Override
    protected void revoke() {
        super.revoke();
    }

    @Override
    protected void validateUpdate() {
        super.validateUpdate();
    }


    public static final class LoanApplicationBuilder extends
            BaseLoanApplicationBuilder<LoanApplicationBuilder> {


        public LoanApplicationBuilder(FeatureConfig featureConfig) {
            super(featureConfig);
        }

        public LoanApplicationBuilder(FeatureConfig featureConfig, LoanApplication other) {
            super(featureConfig, other);
        }

        @Override
        protected LoanApplicationBuilder self() {
            return this;
        }

        @Override
        public LoanApplication validateAndBuild() {
            validateInvariants();
            return new LoanApplication(this);
        }

        private void validateInvariants() {
            Validator.validate(v -> v.appendErrors(validateBaseInvariants()),
                               MorabeheLoanApplicationValidationException::new
            );
        }

    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LoanApplication that)) {
            return false;
        }
        return new EqualsBuilder().append(getId(), that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
    }


}
