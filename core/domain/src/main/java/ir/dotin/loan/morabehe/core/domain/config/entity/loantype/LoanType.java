package ir.dotin.loan.morabehe.core.domain.config.entity.loantype;

import ir.dotin.loan.baseloan.domain.config.entity.loantype.BaseLoanType;
import ir.dotin.loan.baseloan.domain.config.exception.LoanTypeException;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanTypeId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.exception.DomainError;
import ir.dotin.platform.ddd.common.util.Validator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@SuppressWarnings("FieldMayBeFinal")
public final class LoanType extends BaseLoanType {

    private Boolean hasIssueMerchandiseDocument;
    private Set<MorabeheLoanRuleId> loanRuleIds;

    LoanType(LoanTypeBuilder builder) {
        super(builder);
        this.hasIssueMerchandiseDocument = builder.hasIssueMerchandiseDocument;
        this.loanRuleIds = builder.loanRuleIds;
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
        private Set<MorabeheLoanRuleId> loanRuleIds;

        public LoanTypeBuilder() {
        }

        public LoanTypeBuilder(LoanType other) {
            this.hasIssueMerchandiseDocument = other.hasIssueMerchandiseDocument;
            this.loanRuleIds = other.loanRuleIds;
        }

        public LoanTypeBuilder withHasIssueMerchandiseDocument(
                Boolean hasIssueMerchandiseDocument) {
            this.hasIssueMerchandiseDocument = hasIssueMerchandiseDocument;
            return this;
        }

        public LoanTypeBuilder withLoanRuleIds(Set<MorabeheLoanRuleId> loanRuleIds) {
            this.loanRuleIds = loanRuleIds;
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
            List<DomainError> errors = super.validateBaseInvariants();
            Validator.validate(
                    v -> v.checkNotNull(hasIssueMerchandiseDocument, "hasIssueMerchandiseDocument")
                            .checkNotEmpty(loanRuleIds, "loanRuleIds")
                            .appendErrors(errors),
                    LoanTypeException::new
            );
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<MorabeheLoanRuleId> getLoanRuleIds() {
        return loanRuleIds;
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
