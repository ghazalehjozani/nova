package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("list")
public class IssueGeneralDocumentResponse extends FcbBaseResponse {

    @XStreamImplicit(itemFieldName = "TransferMoneyReturnVO")
    private List<TransferMoneyResponse> transferMoneyReturns;

    /** Overall document number generated */
    @XStreamAlias("documentNumber")
    private String documentNumber;

    /** Check if all transfer money returns are successful */
    public boolean isAllItemsSuccessful() {
        if (transferMoneyReturns == null || transferMoneyReturns.isEmpty()) {
            return false;
        }
        return transferMoneyReturns.stream().allMatch(TransferMoneyResponse::isSuccess);
    }

    /** Get count of successful items */
    public long getSuccessfulItemsCount() {
        if (transferMoneyReturns == null) {
            return 0;
        }
        return transferMoneyReturns.stream()
                .filter(TransferMoneyResponse::isSuccess)
                .count();
    }

    /** Get count of failed items */
    public long getFailedItemsCount() {
        if (transferMoneyReturns == null) {
            return 0;
        }
        return transferMoneyReturns.stream()
                .filter(TransferMoneyResponse::isError)
                .count();
    }
}
