package ir.dotin.loan.morabehe.adapters.driven.persistence.loantype.mapper;

import ir.dotin.loan.baseloan.adapters.driven.persistence.loantype.mapper.BaseLoanTypeEntryMapper;
import ir.dotin.loan.morabehe.adapters.driven.persistence.loantype.model.MorabeheLoanTypeEntry;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.LoanType;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class MorabeheLoanTypeEntryMapper extends
        BaseLoanTypeEntryMapper<LoanType, LoanType.LoanTypeBuilder> {

    public MorabeheLoanTypeEntry mapToDocument(MorabeheLoanType loanType) {
        if (loanType == null) {
            return null;
        }

        MorabeheLoanTypeEntry document = new MorabeheLoanTypeEntry();
        document.setId(loanType.id().value().toString());
        document.setHasIssueMerchandiseDocument(loanType.getLoanType()
                                                        .hasIssueMerchandiseDocument());
        document.setLoanRuleIds(mapSet(loanType.getLoanType().loanRuleIds(),
                                       id -> id.value().toString()));
        var baseLoanTypeDocument = super.mapToDocument(loanType.getLoanType());
        document.setLoanType(baseLoanTypeDocument);
        return document;
    }

    public MorabeheLoanType mapToAggregate(MorabeheLoanTypeEntry document) {
        if (document == null) {
            return null;
        }

        var morabeheLoanTypeId = new MorabeheLoanTypeId(UUID.fromString(document.getId()));
        var loanTypeBuilder = new LoanType.LoanTypeBuilder()
                .withLoanRuleIds(mapSet(document.getLoanRuleIds(),
                                        id -> new MorabeheLoanRuleId(UUID.fromString(id))));
        loanTypeBuilder.withHasIssueMerchandiseDocument(
                document.getHasIssueMerchandiseDocument());
        super.mapFromDocument(document.getLoanType(), loanTypeBuilder);
        return new MorabeheLoanType(morabeheLoanTypeId, loanTypeBuilder);
    }
}
