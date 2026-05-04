package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ir.dotin.platform.accounting.document.api.enumeration.MetadataSection;
import ir.dotin.platform.accounting.document.api.model.metadata.ArticleMetadata;
import ir.dotin.platform.accounting.document.api.model.metadata.DestinationDetails;
import ir.dotin.platform.accounting.document.api.model.metadata.NetworkInfo;
import ir.dotin.platform.accounting.document.api.model.metadata.OperationalInfo;
import ir.dotin.platform.accounting.document.api.model.metadata.PartyInfo;
import ir.dotin.platform.accounting.document.api.model.metadata.ProductInfo;
import ir.dotin.platform.accounting.document.api.model.metadata.SourceDetails;
import ir.dotin.platform.accounting.document.api.model.metadata.TerminalInfo;
import ir.dotin.platform.accounting.document.api.model.metadata.ToolInfo;
import ir.dotin.platform.accounting.document.api.model.metadata.TransactionInfo;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ExtraInfoMetadataDto;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ArticleMetadataMapper {

    private static final String DEFAULT_EXTRA_INFO_TYPE = "LOAN_DOCUMENT";

    public static ExtraInfoMetadataDto toDto(ArticleMetadata metadata) {
        if (metadata == null) {
            return null;
        }

        var builder = ExtraInfoMetadataDto.builder().extraInfoType(DEFAULT_EXTRA_INFO_TYPE);

        applyTransactionInfo(builder, metadata.transactionInfo());
        applyTerminalInfo(builder, metadata.terminalInfo());
        applyNetworkInfo(builder, metadata.networkInfo());
        metadata.sourceDetails().ifPresent(s -> applySourceDetails(builder, s));
        metadata.destinationDetails().ifPresent(d -> applyDestinationDetails(builder, d));
        metadata.operationalInfo().ifPresent(o -> applyOperationalInfo(builder, o));
        applyIncludedSections(builder, metadata);

        return builder.build();
    }

    private static void applyTransactionInfo(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, TransactionInfo info) {
        if (info == null) return;
        if (info.transactionType() != null) {
            b.typeCode(info.transactionType().getCode());
        }
        if (info.transactionCause() != null) {
            b.causeTypeCode(info.transactionCause().getCode());
        }
    }

    private static void applyTerminalInfo(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, TerminalInfo info) {
        if (info == null) return;
        b.terminalId(info.terminalId());
        info.terminalTypeCode().ifPresent(b::terminalTypeCode);
        info.terminalManagerCode().ifPresent(b::terminalManagerCode);
        info.terminalManagerName().ifPresent(b::terminalManagerName);
        info.terminalIp().ifPresent(b::terminalIp);
        info.terminalMerchantCategoryCode().ifPresent(b::terminalMerchantCategoryCode);
    }

    private static void applyNetworkInfo(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, NetworkInfo info) {
        if (info == null) return;
        b.networkTypeCode(info.networkTypeCode());
        info.channel().ifPresent(b::channel);
        info.agentName().ifPresent(b::networkAgentName);
        info.agentId().ifPresent(b::networkAgentId);
        info.referenceNumber().ifPresent(b::networkReferenceNumber);
        info.sequenceCounter().ifPresent(b::networkSequenceCounter);
    }

    private static void applySourceDetails(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, SourceDetails details) {
        details.productInfo().ifPresent(p -> applySourceProduct(b, p));
        details.owner().ifPresent(p -> applySourceOwner(b, p));
        details.originator().ifPresent(p -> applySourceOriginator(b, p));
        details.toolInfo().ifPresent(t -> applySourceTool(b, t));
    }

    private static void applySourceProduct(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, ProductInfo info) {
        b.srcProductId(info.productId());
        info.productCode().ifPresent(b::srcProductCode);
        info.productTypeCode().ifPresent(b::srcProductTypeCode);
        info.productBankCode().ifPresent(b::srcProductBankCode);
    }

    private static void applySourceOwner(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, PartyInfo info) {
        info.name().ifPresent(b::srcOwnerName);
        b.srcOwnerIds(combineIds(info.primaryId(), info.additionalIds()));
    }

    private static void applySourceOriginator(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, PartyInfo info) {
        info.name().ifPresent(b::srcOriginatorName);
        b.srcOriginatorIds(combineIds(info.primaryId(), info.additionalIds()));
    }

    private static void applySourceTool(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, ToolInfo info) {
        b.srcToolId(info.toolId());
        info.toolTypeCode().ifPresent(b::srcToolTypeCode);
    }

    private static void applyDestinationDetails(
            ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, DestinationDetails details) {
        details.productInfo().ifPresent(p -> applyDestProduct(b, p));
        details.receiver().ifPresent(p -> applyDestReceiver(b, p));
        details.beneficiary().ifPresent(p -> applyDestBeneficiary(b, p));
        details.toolInfo().ifPresent(t -> applyDestTool(b, t));
    }

    private static void applyDestProduct(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, ProductInfo info) {
        b.destProductId(info.productId());
        info.productCode().ifPresent(b::destProductCode);
        info.productTypeCode().ifPresent(b::destProductTypeCode);
        info.productBankCode().ifPresent(b::destProductBankCode);
    }

    private static void applyDestReceiver(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, PartyInfo info) {
        info.name().ifPresent(b::destReceiverName);
        b.destReceiverIds(combineIds(info.primaryId(), info.additionalIds()));
    }

    private static void applyDestBeneficiary(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, PartyInfo info) {
        info.name().ifPresent(b::destBeneficiaryName);
        b.destBeneficiaryIds(combineIds(info.primaryId(), info.additionalIds()));
    }

    private static void applyDestTool(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, ToolInfo info) {
        b.destToolId(info.toolId());
        info.toolTypeCode().ifPresent(b::destToolTypeCode);
    }

    private static void applyOperationalInfo(ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, OperationalInfo info) {
        info.fraudMessageId().ifPresent(b::messageId);
        info.hasAcceptedDoc().ifPresent(b::hasAcceptedDoc);
        info.reversedTrxId().ifPresent(b::reversedTrx);
        if (info.soc() != null && !info.soc().isEmpty()) {
            b.socList(List.copyOf(info.soc()));
        }
    }

    private static void applyIncludedSections(
            ExtraInfoMetadataDto.ExtraInfoMetadataDtoBuilder b, ArticleMetadata metadata) {
        Set<String> sections = new LinkedHashSet<>();
        sections.add(MetadataSection.TERMINAL_INFO.name());
        sections.add(MetadataSection.NETWORK_INFO.name());
        if (metadata.transactionInfo() != null) {
            sections.add(MetadataSection.TRANSACTION_INFO.name());
        }
        metadata.sourceDetails().ifPresent(s -> sections.add(MetadataSection.SOURCE_DETAILS_ALL.name()));
        metadata.destinationDetails().ifPresent(d -> sections.add(MetadataSection.DESTINATION_DETAILS_ALL.name()));
        metadata.operationalInfo().ifPresent(o -> sections.add(MetadataSection.OPERATIONAL_INFO.name()));
        b.includedSections(sections);
    }

    private static List<String> combineIds(String primaryId, List<String> additionalIds) {
        List<String> combined = new ArrayList<>();
        Optional.ofNullable(primaryId).filter(s -> !s.isBlank()).ifPresent(combined::add);
        if (additionalIds != null) {
            additionalIds.stream().filter(s -> s != null && !s.isBlank()).forEach(combined::add);
        }
        return List.copyOf(combined);
    }
}
