package ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.CollateralSerial;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;
import ir.dotin.loan.morabehe.core.domain.loanapplication.event.*;
import ir.dotin.loan.morabehe.core.domain.loanapplication.exception.MorabeheLoanApplicationValidationException;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.entity.AggregateRoot;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static ir.dotin.platform.ddd.common.util.Validator.validate;

@SuppressWarnings("FieldMayBeFinal")
public class MorabeheLoanApplication extends AggregateRoot<MorabeheLoanApplicationId> {

    private LoanApplication loanApplication;

    public MorabeheLoanApplication(MorabeheLoanApplicationId morabeheLoanApplicationId,
                                   LoanApplication loanApplication) {
        super(morabeheLoanApplicationId);
        validate(v -> v.checkNotNull(loanApplication, "loanApplicationBuilder"),
                 MorabeheLoanApplicationValidationException::new);
        this.loanApplication = loanApplication;
    }

    public void request(ApplicationNumber applicationNumber) {
        loanApplication.request(applicationNumber);
        setId(MorabeheLoanApplicationId.generate());
        registerEvent(MorabeheLoanApplicationRequestedEvent.of(id()));
    }

    public void approve(SanctionSerial sanctionSerial) {
        loanApplication.approve(sanctionSerial);
        registerEvent(MorabeheLoanApplicationApprovedEvent.of(id()));
    }

    public void addCollateral(CollateralSerial collateralSerial) {
        loanApplication.addCollateral(collateralSerial);
        registerEvent(MorabeheLoanApplicationCollateralAddedEvent.of(id()));
    }

    public void issueContract() {
        loanApplication.issueContract();
        registerEvent(MorabeheLoanApplicationContractIssuedEvent.of(id()));
    }

    public void disburse() {
        loanApplication.disburse();
        registerEvent(MorabeheLoanApplicationDisbursedEvent.of(id()));
    }

    public void revoke() {
        loanApplication.revoke();
        registerEvent(MorabeheLoanApplicationRevokedEvent.of(id()));
    }

    public void validateUpdate() {
        loanApplication.validateUpdate();
        registerEvent(MorabeheLoanApplicationUpdatedEvent.of(id()));
    }


    public LoanApplication loanApplication() {
        return loanApplication;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MorabeheLoanApplication that)) {
            return false;
        }
        return new EqualsBuilder().append(id(), that.id()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id()).toHashCode();
    }

}
