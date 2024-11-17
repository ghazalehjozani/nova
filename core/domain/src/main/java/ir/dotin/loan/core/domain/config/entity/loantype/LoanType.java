package ir.dotin.loan.core.domain.config.entity.loantype;

import ir.dotin.loan.baseloan.domain.config.entity.loantype.BaseLoanType;
import ir.dotin.loan.baseloan.domain.config.exception.LoanTypeException;
import ir.dotin.loan.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.exception.DomainError;
import ir.dotin.platform.ddd.common.util.Validator;
import java.util.List;
import java.util.Set;

public class LoanType extends BaseLoanType {

    private Boolean hasIssueMerchandiseDocument;
    private Set<MorabeheLoanRuleId> loanRuleIds;

    protected LoanType(LoanTypeBuilder builder) {
        super(builder);
        this.hasIssueMerchandiseDocument = builder.hasIssueMerchandiseDocument;
        this.loanRuleIds = builder.loanRuleIds;
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
}
