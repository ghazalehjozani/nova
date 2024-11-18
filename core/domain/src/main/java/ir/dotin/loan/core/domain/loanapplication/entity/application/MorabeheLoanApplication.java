package ir.dotin.loan.core.domain.loanapplication.entity.application;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.CollateralSerial;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;
import ir.dotin.loan.core.domain.loanapplication.entity.application.LoanApplication.LoanApplicationBuilder;
import ir.dotin.loan.core.domain.loanapplication.event.MorabeheLoanApplicationApprovedEvent;
import ir.dotin.loan.core.domain.loanapplication.event.MorabeheLoanApplicationCollateralAddedEvent;
import ir.dotin.loan.core.domain.loanapplication.event.MorabeheLoanApplicationContractIssuedEvent;
import ir.dotin.loan.core.domain.loanapplication.event.MorabeheLoanApplicationDisbursedEvent;
import ir.dotin.loan.core.domain.loanapplication.event.MorabeheLoanApplicationRequestedEvent;
import ir.dotin.loan.core.domain.loanapplication.event.MorabeheLoanApplicationRevokedEvent;
import ir.dotin.loan.core.domain.loanapplication.event.MorabeheLoanApplicationUpdatedEvent;
import ir.dotin.loan.core.domain.loanapplication.exception.MorabeheLoanApplicationException;
import ir.dotin.loan.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.entity.AggregateRoot;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static ir.dotin.platform.ddd.common.util.Validator.validate;

@SuppressWarnings("FieldMayBeFinal")
public class MorabeheLoanApplication extends AggregateRoot<MorabeheLoanApplicationId> {

    private LoanApplication loanApplication;

    public MorabeheLoanApplication(MorabeheLoanApplicationId morabeheLoanApplicationId,
                                   LoanApplicationBuilder loanApplicationBuilder) {
        super(morabeheLoanApplicationId);
        validate(v -> v.checkNotNull(loanApplicationBuilder, "loanApplicationBuilder"),
                 MorabeheLoanApplicationException::new);
        loanApplication = loanApplicationBuilder.validateAndBuild();
    }

    public void request(ApplicationNumber applicationNumber) {
        loanApplication.request(applicationNumber);
        setId(MorabeheLoanApplicationId.generate());
        registerEvent(MorabeheLoanApplicationRequestedEvent.of(getId()));
    }

    public void approve(SanctionSerial sanctionSerial) {
        loanApplication.approve(sanctionSerial);
        registerEvent(MorabeheLoanApplicationApprovedEvent.of(getId()));
    }

    public void addCollateral(CollateralSerial collateralSerial) {
        loanApplication.addCollateral(collateralSerial);
        registerEvent(MorabeheLoanApplicationCollateralAddedEvent.of(getId()));
    }

    public void issueContract() {
        loanApplication.issueContract();
        registerEvent(MorabeheLoanApplicationContractIssuedEvent.of(getId()));
    }

    public void disburse() {
        loanApplication.disburse();
        registerEvent(MorabeheLoanApplicationDisbursedEvent.of(getId()));
    }

    public void revoke() {
        loanApplication.revoke();
        registerEvent(MorabeheLoanApplicationRevokedEvent.of(getId()));
    }

    public void validateUpdate() {
        loanApplication.validateUpdate();
        registerEvent(MorabeheLoanApplicationUpdatedEvent.of(getId()));
    }


    public LoanApplication getLoanApplication() {
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
        return new EqualsBuilder().append(getId(), that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
    }

}
