package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import lombok.Data;

@Data
@XStreamAlias("list")
public class IssueGeneralDocumentResponse {

    @XStreamImplicit(itemFieldName = "com.fanap.business.deposit.service.valueobjects.TransferMoneyReturnVO")
    private List<TransferMoneyResponse> transferMoneyReturns;

    public long getSuccessfulItemsCount() {
        if (transferMoneyReturns == null) {
            return 0;
        }
        return transferMoneyReturns.stream()
                .filter(TransferMoneyResponse::isSuccess)
                .count();
    }

    public long getFailedItemsCount() {
        if (transferMoneyReturns == null) {
            return 0;
        }
        return transferMoneyReturns.stream()
                .filter(TransferMoneyResponse::isError)
                .count();
    }
}
