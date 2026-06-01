package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtraInfoMetadataDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String extraInfoType;

    private String messageId;
    private String typeCode;
    private String causeTypeCode;
    private String hasAcceptedDoc;
    private String reversedTrx;

    @Builder.Default
    private List<String> socList = List.of();

    private String terminalTypeCode;
    private String terminalId;
    private String terminalManagerCode;
    private String terminalManagerName;
    private String terminalIp;
    private String terminalMerchantCategoryCode;

    private String networkTypeCode;
    private String channel;
    private String networkAgentName;
    private String networkAgentId;
    private String networkReferenceNumber;
    private String networkSequenceCounter;

    private String srcProductCode;
    private String srcProductTypeCode;
    private String srcProductId;
    private String srcProductBankCode;
    private String srcOwnerName;

    @Builder.Default
    private List<String> srcOwnerIds = List.of();

    private String srcOriginatorName;

    @Builder.Default
    private List<String> srcOriginatorIds = List.of();

    private String srcToolTypeCode;
    private String srcToolId;
    private String srcPinValidationTypeCode;
    private String srcPinTypeCode;

    private String destProductCode;
    private String destProductTypeCode;
    private String destProductId;
    private String destProductBankCode;
    private String destReceiverName;

    @Builder.Default
    private List<String> destReceiverIds = List.of();

    private String destBeneficiaryName;

    @Builder.Default
    private List<String> destBeneficiaryIds = List.of();

    private String destToolTypeCode;
    private String destToolId;

    private String clientId;
    private String clientName;

    private String bankCode;
    private String bankName;

    @Builder.Default
    private Set<String> includedSections = new LinkedHashSet<>();
}
