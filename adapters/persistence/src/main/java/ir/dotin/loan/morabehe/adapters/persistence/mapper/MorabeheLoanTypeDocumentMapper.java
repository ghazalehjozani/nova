package ir.dotin.loan.morabehe.adapters.persistence.mapper;

import ir.dotin.loan.baseloan.adapters.persistence.mapper.BaseLoanTypeDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanTypeDocument;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.LoanType;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;


@Component
public class MorabeheLoanTypeDocumentMapper extends
        BaseLoanTypeDocumentMapper<LoanType, LoanType.LoanTypeBuilder> {

    public MorabeheLoanTypeDocument mapToDocument(MorabeheLoanType loanType) {
        if (loanType == null) {
            return null;
        }

        MorabeheLoanTypeDocument morabeheLoanTypeDocument = new MorabeheLoanTypeDocument();
        morabeheLoanTypeDocument.setId(loanType.getId().value());
        morabeheLoanTypeDocument.setHasIssueMerchandiseDocument(loanType.getLoanType()
                                                                        .getHasIssueMerchandiseDocument());
        morabeheLoanTypeDocument.setLoanRuleIds(loanType.getLoanType().getLoanRuleIds()
                                                        .stream()
                                                        .map(MorabeheLoanRuleId::value)
                                                        .collect(Collectors.toSet()));
        var baseLoanTypeDocument = super.mapToDocument(loanType.getLoanType());
        morabeheLoanTypeDocument.setLoanType(baseLoanTypeDocument);
        return morabeheLoanTypeDocument;
    }

    public MorabeheLoanType mapToAggregate(MorabeheLoanTypeDocument loanTypeDocument) {
        if (loanTypeDocument == null) {
            return null;
        }

        var loanTypeBuilder = new LoanType.LoanTypeBuilder();
        var morabeheLoanTypeId = new MorabeheLoanTypeId(loanTypeDocument.getId());
        loanTypeBuilder.withLoanRuleIds(
                loanTypeDocument.getLoanRuleIds()
                        .stream()
                        .map(MorabeheLoanRuleId::new)
                        .collect(Collectors.toSet()));
        loanTypeBuilder.withHasIssueMerchandiseDocument(
                loanTypeDocument.getHasIssueMerchandiseDocument());
        super.mapFromDocument(loanTypeDocument.getLoanType(), loanTypeBuilder);
        return new MorabeheLoanType(morabeheLoanTypeId, loanTypeBuilder);
    }
}
