package ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application;

import ir.dotin.loan.baseloan.domain.loanapplication.entity.application.BaseLoanApplication;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.CollateralSerial;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;
import ir.dotin.loan.baseloan.domain.shared.exception.BaseErrorMessages;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Money;
import ir.dotin.loan.morabehe.core.domain.loanapplication.exception.MorabeheLoanApplicationValidationException;
import ir.dotin.platform.ddd.common.exception.ValidationError;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import ir.dotin.platform.ddd.common.util.Validator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class LoanApplication extends BaseLoanApplication {

    private Money prePaymentAmount;
    private String prePaymentDepositNumber;


    LoanApplication(LoanApplicationBuilder builder) {
        super(builder);
        this.prePaymentAmount = builder.prePaymentAmount;
        this.prePaymentDepositNumber = builder.prePaymentDepositNumber;
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

        private Money prePaymentAmount;
        private String prePaymentDepositNumber;

        public LoanApplicationBuilder withPrePaymentAmount(Money prePaymentAmount) {
            this.prePaymentAmount = prePaymentAmount;
            return this;
        }

        public LoanApplicationBuilder withPrePaymentDepositNumber(String prePaymentDepositNumber) {
            this.prePaymentDepositNumber = prePaymentDepositNumber;
            return this;
        }

        public LoanApplicationBuilder(FeatureConfig featureConfig) {
            super(featureConfig);
        }

        public LoanApplicationBuilder(FeatureConfig featureConfig, LoanApplication other,
                                      Money prePaymentAmount, String prePaymentDepositNumber) {
            super(featureConfig, other);
            this.prePaymentAmount = prePaymentAmount;
            this.prePaymentDepositNumber = prePaymentDepositNumber;
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
            Validator.validate(v -> v.appendErrors(validateBaseInvariants())
                            .appendErrors(validateMorabeheInvariants()),
                    MorabeheLoanApplicationValidationException::new
            );
        }

        private List<ValidationError> validateMorabeheInvariants() {
            List<ValidationError> errors = new ArrayList<>();
            if (prePaymentAmount != null && prePaymentAmount.isGreaterThan(Money.zero())
                    && prePaymentDepositNumber == null) {
                errors.add(new ValidationError(BaseErrorMessages.ValidationErrors.NOT_NULL,
                        "prePaymentDepositNumber"));
            }
            return errors;
        }
    }

    public Money getPrePaymentAmount() {
        return prePaymentAmount;
    }

    public String getPrePaymentDepositNumber() {
        return prePaymentDepositNumber;
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
