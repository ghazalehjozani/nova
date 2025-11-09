package ir.dotin.loan.trade.core.application.service.shared.util;

import java.util.List;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.factory.DocumentMetadataFactory;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.ArticleMetadata;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.OperationalInfo;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DocumentMetadataUtils {

    public Result<ArticleMetadata> createBaseArticleMetadata(
            TradeLoanFacility facility, TradeLoanType loanType, BranchCode branchCode, TransactionConfig config) {

        return DocumentMetadataFactory.builder()
                .terminal(DocumentMetadataFactory.TerminalConfig.of(
                        config.terminalType(), branchCode.value(), config.terminalIp()))
                .product(DocumentMetadataFactory.ProductConfig.of(
                        config.productCode(),
                        loanType.getCode().value(),
                        facility.getLoanApplication()
                                .getApplicationNumber()
                                .get()
                                .formattedApplicationNumber()))
                .party(DocumentMetadataFactory.PartyConfig.of(
                        facility.getLoanApplication().getCustomer().customerNumber(),
                        facility.getLoanApplication().getCustomer().name().fullName(),
                        List.of()))
                .tool(DocumentMetadataFactory.ToolConfig.of(config.userId(), config.toolSource()))
                .network(DocumentMetadataFactory.NetworkConfig.of(config.networkType(), config.channel()))
                .operational(OperationalInfo.builder().build())
                .build();
    }
}
