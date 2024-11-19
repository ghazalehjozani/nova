package ir.dotin.loan.morabehe.core.domain.config.entity.loantype;


import ir.dotin.loan.baseloan.domain.config.valueobject.LoanTypeId;
import ir.dotin.loan.morabehe.core.domain.config.entity.exception.MorabeheLoanTypeException;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.LoanType.LoanTypeBuilder;
import ir.dotin.loan.morabehe.core.domain.config.event.MorabeheLoanTypeCreatedEvent;
import ir.dotin.loan.morabehe.core.domain.config.event.MorabeheLoanTypeDisabledEvent;
import ir.dotin.loan.morabehe.core.domain.config.event.MorabeheLoanTypeUpdatedEvent;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.platform.ddd.common.entity.AggregateRoot;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static ir.dotin.platform.ddd.common.util.Validator.validate;

@SuppressWarnings("FieldMayBeFinal")
public class MorabeheLoanType extends AggregateRoot<MorabeheLoanTypeId> {

    private LoanType loanType;

    public MorabeheLoanType(MorabeheLoanTypeId morabeheLoanTypeId,
                            LoanTypeBuilder loanTypeBuilder) {
        super(morabeheLoanTypeId);
        validate(v -> v.checkNotNull(loanTypeBuilder, "loanTypeBuilder"),
                 MorabeheLoanTypeException::new);
        loanType = loanTypeBuilder.validateAndBuild();
    }

    public void createLoanType() {
        setId(MorabeheLoanTypeId.generate());
        loanType.createLoanType();
        registerEvent(MorabeheLoanTypeCreatedEvent.of(getId()));
    }

    public void markAsDisabled() {
        loanType.markAsDisabled();
        registerEvent(MorabeheLoanTypeDisabledEvent.of(getId()));
    }

    public void activate() {
        loanType.activate();
    }

    public void deactivate() {
        loanType.deactivate();
    }

    public void validateIsEnable() {
        loanType.validateIsEnable();
    }

    public void setPreviousVersion(LoanTypeId id) {
        loanType.setPreviousVersion(id);
        registerEvent(MorabeheLoanTypeUpdatedEvent.of(this.getId(), id));
    }

    public void validateIsActive() {
        loanType.validateIsActive();
    }


    public LoanType getLoanType() {
        return loanType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MorabeheLoanType that)) {
            return false;
        }
        return new EqualsBuilder().append(getId(), that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
    }

}
