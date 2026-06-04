package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class PostTransactionRequest extends FcbKafkaBaseRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Builder.Default
    @JsonIgnore
    private String operationName = "issue-general-document";

    private final String transactionId;
    private final String comment;
    private final String isoCode;
    private final String branchCode;
    private final String transferMoneyBillNumber;
    private final String documentTemplateCode;
    private final Boolean skipTransferMoneyBillNumber;
    private final Map<String, Object> templateObject;

    @Builder.Default
    private final List<DocumentItemDto> items = List.of();

    private final ExtraInfoMetadataDto documentMetadata;
}
