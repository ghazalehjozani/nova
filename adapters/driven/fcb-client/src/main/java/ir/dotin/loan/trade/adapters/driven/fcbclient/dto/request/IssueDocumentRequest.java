package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueDocumentRequest {

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

    private ExtraInfoVO documentExtraInfo;

    private List<ExtraInfoVO> documentItemExtraInfoList;

    public void validate() {
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
        if (documentItemExtraInfoList != null && !documentItemExtraInfoList.isEmpty()) {
            if (documentItemExtraInfoList.size() != items.size()) {
                throw new IllegalArgumentException(String.format(
                        "documentItemExtraInfoList size (%d) must match items size (%d)",
                        documentItemExtraInfoList.size(), items.size()));
            }
        }
    }
}
