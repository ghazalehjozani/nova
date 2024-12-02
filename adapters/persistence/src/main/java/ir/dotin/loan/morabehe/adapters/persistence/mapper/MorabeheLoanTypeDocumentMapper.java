package ir.dotin.loan.morabehe.adapters.persistence.mapper;

import ir.dotin.loan.baseloan.adapters.persistence.mapper.BaseLoanTypeDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanTypeDocument;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.LoanType;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import java.util.UUID;
import org.springframework.stereotype.Component;


@Component
public class MorabeheLoanTypeDocumentMapper extends
        BaseLoanTypeDocumentMapper<LoanType, LoanType.LoanTypeBuilder> {

    public MorabeheLoanTypeDocument mapToDocument(MorabeheLoanType loanType) {
        if (loanType == null) {
            return null;
        }

        MorabeheLoanTypeDocument document = new MorabeheLoanTypeDocument();
        document.setId(loanType.getId().value().toString());
        document.setHasIssueMerchandiseDocument(loanType.getLoanType()
                                                        .getHasIssueMerchandiseDocument());
        document.setLoanRuleIds(mapSet(loanType.getLoanType().getLoanRuleIds(),
                                       id -> id.value().toString()));
        var baseLoanTypeDocument = super.mapToDocument(loanType.getLoanType());
        document.setLoanType(baseLoanTypeDocument);
        return document;
    }

    public MorabeheLoanType mapToAggregate(MorabeheLoanTypeDocument document) {
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
