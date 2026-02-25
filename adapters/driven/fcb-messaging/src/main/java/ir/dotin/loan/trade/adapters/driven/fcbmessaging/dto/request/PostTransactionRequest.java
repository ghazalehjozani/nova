package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@EqualsAndHashCode(callSuper = true)
public class PostTransactionRequest extends FcbKafkaBaseRequest {

    @Builder.Default
    private String transactionId = UUID.randomUUID().toString();

    private String comment;

    private String isoCode;

    private String branchCode;

    private String transferMoneyBillNumber;

    private String documentTemplateCode;

    @Builder.Default
    private Boolean skipTransferMoneyBillNumber = false;

    private Map<String, Object> templateObject;

    private List<String> items;

    private List<String> itemComments;

    private List<String> transferMoneyBills;

    private List<String> itemSOCs;

    private String documentExtraInfo;

    private List<ExtraInfoVO> documentItemExtraInfoList;

    public PostTransactionRequest() {
        super("issue-general-document");
    }

    public PostTransactionRequest(
            String transactionId,
            String comment,
            String isoCode,
            String branchCode,
            String transferMoneyBillNumber,
            String documentTemplateCode,
            Boolean skipTransferMoneyBillNumber,
            Map<String, Object> templateObject,
            List<String> items,
            List<String> itemComments,
            List<String> transferMoneyBills,
            List<String> itemSOCs,
            String documentExtraInfo,
            List<ExtraInfoVO> documentItemExtraInfoList) {
        super("issue-general-document");
        this.transactionId = transactionId;
        this.comment = comment;
        this.isoCode = isoCode;
        this.branchCode = branchCode;
        this.transferMoneyBillNumber = transferMoneyBillNumber;
        this.documentTemplateCode = documentTemplateCode;
        this.skipTransferMoneyBillNumber = skipTransferMoneyBillNumber;
        this.templateObject = templateObject;
        this.items = items;
        this.itemComments = itemComments;
        this.transferMoneyBills = transferMoneyBills;
        this.itemSOCs = itemSOCs;
        this.documentExtraInfo = documentExtraInfo;
        this.documentItemExtraInfoList = documentItemExtraInfoList;
    }

    public void validate() {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Comment is required");
        }
        if (isoCode == null || isoCode.isBlank()) {
            throw new IllegalArgumentException("ISO code is required");
        }
        if (branchCode == null || branchCode.isBlank()) {
            throw new IllegalArgumentException("Branch code is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be empty");
        }
        if (itemComments == null || itemComments.isEmpty()) {
            throw new IllegalArgumentException("Item comments list cannot be empty");
        }
        if (items.size() != itemComments.size()) {
            throw new IllegalArgumentException(String.format(
                    "Items size (%d) must match itemComments size (%d)", items.size(), itemComments.size()));
        }
    }
}
