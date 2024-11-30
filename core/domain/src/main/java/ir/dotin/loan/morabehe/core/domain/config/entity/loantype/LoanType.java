package ir.dotin.loan.morabehe.core.domain.config.entity.loantype;

import ir.dotin.loan.baseloan.domain.config.entity.loantype.BaseLoanType;
import ir.dotin.loan.baseloan.domain.config.exception.LoanTypeValidationException;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanTypeId;
import ir.dotin.platform.ddd.common.exception.ValidationError;
import ir.dotin.platform.ddd.common.util.Validator;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@SuppressWarnings("FieldMayBeFinal")
public final class LoanType extends BaseLoanType {

    private Boolean hasIssueMerchandiseDocument;

    LoanType(LoanTypeBuilder builder) {
        super(builder);
        this.hasIssueMerchandiseDocument = builder.hasIssueMerchandiseDocument;
    }

    @Override
    protected void createLoanType() {
        super.createLoanType();
    }


    @Override
    protected void markAsDisabled() {
        super.markAsDisabled();
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
    protected void validateIsEnable() {
        super.validateIsEnable();
    }

    @Override
    protected void setPreviousVersion(LoanTypeId id) {
        super.setPreviousVersion(id);
    }

    @Override
    protected void validateIsActive() {
        super.validateIsActive();
    }

    public static class LoanTypeBuilder extends BaseLoanTypeBuilder<LoanTypeBuilder> {

        private Boolean hasIssueMerchandiseDocument;

        public LoanTypeBuilder() {
        }

        public LoanTypeBuilder(LoanType other) {
            this.hasIssueMerchandiseDocument = other.hasIssueMerchandiseDocument;
        }

        public LoanTypeBuilder withHasIssueMerchandiseDocument(
                Boolean hasIssueMerchandiseDocument) {
            this.hasIssueMerchandiseDocument = hasIssueMerchandiseDocument;
            return this;
        }

        @Override
        protected LoanTypeBuilder self() {
            return this;
        }

        @Override
        public LoanType validateAndBuild() {
            validateInvariants();
            return new LoanType(this);
        }

        private void validateInvariants() {
            List<ValidationError> errors = super.validateBaseInvariants();
            Validator.validate(
                    v -> v.checkNotNull(hasIssueMerchandiseDocument, "hasIssueMerchandiseDocument")
                            .appendErrors(errors),
                    LoanTypeValidationException::new
            );
        }

    }

    public Boolean getHasIssueMerchandiseDocument() {
        return hasIssueMerchandiseDocument;
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
