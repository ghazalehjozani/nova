package ir.dotin.loan.core.domain.loanapplication.entity.application;

import ir.dotin.loan.baseloan.domain.loanapplication.entity.application.BaseLoanApplication;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.CollateralSerial;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;
import ir.dotin.loan.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.loan.core.domain.loanapplication.exception.MorabeheLoanApplicationException;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import ir.dotin.platform.ddd.common.util.Validator;

@SuppressWarnings("FieldMayBeFinal")
public class LoanApplication extends BaseLoanApplication {

    private MorabeheLoanTypeId loanTypeId;
    private MorabeheLoanRuleId loanRuleId;


    LoanApplication(LoanApplicationBuilder builder) {
        super(builder);
        this.loanTypeId = builder.loanTypeId;
        this.loanRuleId = builder.loanRuleId;
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

        private MorabeheLoanTypeId loanTypeId;
        private MorabeheLoanRuleId loanRuleId;

        public LoanApplicationBuilder(FeatureConfig featureConfig) {
            super(featureConfig);
        }

        public LoanApplicationBuilder(FeatureConfig featureConfig, LoanApplication other) {
            super(featureConfig, other);
            this.loanRuleId = other.loanRuleId;
            this.loanTypeId = other.loanTypeId;
        }

        public LoanApplicationBuilder withLoanTypeId(MorabeheLoanTypeId loanTypeId) {
            this.loanTypeId = loanTypeId;
            return this;
        }

        public LoanApplicationBuilder withLoanRuleId(MorabeheLoanRuleId loanRuleId) {
            this.loanRuleId = loanRuleId;
            return this;
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
            Validator.validate(
                    v -> v.checkNotNull(loanTypeId, "loanTypeId")
                            .checkNotNull(loanRuleId, "loanRuleId")
                            .appendErrors(validateBaseInvariants()),
                    MorabeheLoanApplicationException::new
            );
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public MorabeheLoanTypeId getLoanTypeId() {
        return loanTypeId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MorabeheLoanRuleId getLoanRuleId() {
        return loanRuleId;
    }

}
