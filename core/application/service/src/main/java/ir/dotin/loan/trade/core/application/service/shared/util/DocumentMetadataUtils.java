package ir.dotin.loan.trade.core.application.service.shared.util;

import java.util.List;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.accounting.document.api.model.metadata.ArticleMetadata;
import ir.dotin.platform.accounting.document.api.model.metadata.OperationalInfo;
import ir.dotin.platform.accounting.document.core.factory.DocumentMetadataFactory;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DocumentMetadataUtils {

    public Result<ArticleMetadata> createBaseArticleMetadata(
            TradeLoanFacility facility, TradeLoanType loanType, BranchCode branchCode, TransactionConfig config) {

        return DocumentMetadataFactory.builder()
                .terminal(DocumentMetadataFactory.TerminalConfig.of(
                        config.terminalType(), config.terminalId(), config.terminalIp()))
                .product(DocumentMetadataFactory.ProductConfig.of(
                        config.productCode(),
                        loanType.getCode().value(),
                        facility.getLoanApplication()
                                .getApplicationNumber()
                                .get()
                                .formattedApplicationNumber()))
                .party(DocumentMetadataFactory.PartyConfig.of(
                        facility.getLoanApplication().getApplicant().customerNumber(),
                        facility.getLoanApplication().getApplicant().name().fullName(),
                        List.of()))
                .tool(DocumentMetadataFactory.ToolConfig.of(config.userId(), config.toolSource()))
                .network(DocumentMetadataFactory.NetworkConfig.of(config.networkType(), config.channel()))
                .operational(OperationalInfo.builder().build())
                .build();
    }
}
