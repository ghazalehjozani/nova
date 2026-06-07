package ir.dotin.loan.trade.core.application.service.shared.util;

import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.accounting.document.api.model.metadata.ArticleMetadata;
import ir.dotin.platform.accounting.document.api.model.metadata.OperationalInfo;
import ir.dotin.platform.accounting.document.core.factory.DocumentMetadataFactory;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DocumentMetadataType;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DocumentMetadataUtils {

    public Result<ArticleMetadata> createBaseArticleMetadata(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            BranchCode branchCode,
            TransactionConfig config,
            DocumentMetadataType metadataType) {

        return DocumentMetadataFactory.builder()
                .metadataType(metadataType.code())
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

    /**
     * Extra-info fields (terminal/network/tool/product) are optional now that FCB does not consume document metadata
     * (gated by {@code nova.fcb.documents.enabled}). Command fields may be null when a client omits them; this coalesces
     * a missing value to an empty string at the {@link TransactionConfig} boundary so its {@code @NonNull} components
     * hold, while the metadata is stripped before sending whenever the flag is off.
     */
    public String orEmpty(@Nullable String value) {
        return value == null ? "" : value;
    }
}
