package ir.dotin.loan.trade.core.application.service.shared.util;

import java.util.List;
import java.util.Objects;

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
                        Objects.requireNonNull(config.terminalType(), "terminalType"),
                        Objects.requireNonNull(config.terminalId(), "terminalId"),
                        Objects.requireNonNull(config.terminalIp(), "terminalIp")))
                .product(DocumentMetadataFactory.ProductConfig.of(
                        Objects.requireNonNull(config.productCode(), "productCode"),
                        loanType.getCode().value(),
                        facility.getLoanApplication()
                                .getApplicationNumber()
                                .get()
                                .formattedApplicationNumber()))
                .party(DocumentMetadataFactory.PartyConfig.of(
                        facility.getLoanApplication().getApplicant().customerNumber(),
                        facility.getLoanApplication().getApplicant().name().fullName(),
                        List.of()))
                .tool(DocumentMetadataFactory.ToolConfig.of(
                        Objects.requireNonNull(config.userId(), "userId"),
                        Objects.requireNonNull(config.toolSource(), "toolSource")))
                .network(DocumentMetadataFactory.NetworkConfig.of(
                        Objects.requireNonNull(config.networkType(), "networkType"),
                        Objects.requireNonNull(config.channel(), "channel")))
                .operational(OperationalInfo.builder().build())
                .build();
    }
}
