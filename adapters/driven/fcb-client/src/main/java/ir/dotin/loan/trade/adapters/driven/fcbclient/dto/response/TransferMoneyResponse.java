package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransferMoneyResponse extends FcbBaseResponse {

    @XStreamAlias("depositNumber")
    private String depositNumber;

    @XStreamAlias("depositIBAN")
    private String depositIBAN;

    @XStreamAlias("depositType")
    private DepositTypeResponse depositType;

    @XStreamAlias("currentAmount")
    private String currentAmount;

    @XStreamAlias("currentWithdrawableAmount")
    private String currentWithdrawableAmount;

    @XStreamAlias("currencyVO")
    private TransferMoneyCurrencyResponse currencyVO;

    @XStreamAlias("mainAccountAmount")
    private BigDecimal mainAccountAmount;

    @XStreamAlias("blockedAccountAndInterestBlockedAccountAmount")
    private BigDecimal blockedAccountAndInterestBlockedAccountAmount;

    @XStreamImplicit(itemFieldName = "innerTransferMoneyReturns")
    private List<TransferMoneyResponse> innerTransferMoneyReturns;

    @XStreamAlias("identifier")
    private String identifier;

    @XStreamAlias("parentIdentifier")
    private String parentIdentifier;

    @XStreamAlias("documentNumber")
    private String documentNumber;

    @XStreamAlias("referenceNumber")
    private String referenceNumber;

    private Map<String, Object> otherValues = new HashMap<>();

    public void setCurrentAmount(BigDecimal currentAmount) {
        if (currentAmount != null) {
            NumberFormat formatter = DecimalFormat.getInstance();
            formatter.setGroupingUsed(false);
            this.currentAmount = formatter.format(currentAmount);
        } else {
            this.currentAmount = "";
        }
    }

    public void setCurrentWithdrawableAmount(BigDecimal currentWithdrawableAmount) {
        if (currentWithdrawableAmount != null) {
            if (currentWithdrawableAmount.compareTo(BigDecimal.ZERO) < 0) {
                currentWithdrawableAmount = BigDecimal.ZERO;
            }
            NumberFormat formatter = DecimalFormat.getInstance();
            formatter.setGroupingUsed(false);
            this.currentWithdrawableAmount = formatter.format(currentWithdrawableAmount);
        } else {
            this.currentWithdrawableAmount = "0";
        }
    }

    public BigDecimal getCurrentAmountAsBigDecimal() {
        if (currentAmount == null || currentAmount.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(currentAmount);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getCurrentWithdrawableAmountAsBigDecimal() {
        if (currentWithdrawableAmount == null || currentWithdrawableAmount.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(currentWithdrawableAmount);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        this.otherValues.put("documentNumber", documentNumber);
    }

    public void setEndToEndId(String endToEndId) {
        this.referenceNumber = endToEndId;
        this.otherValues.put("referenceNumber", endToEndId);
    }

    /** Get other value by key */
    public Object getOtherValue(String key) {
        return otherValues.get(key);
    }

    /** Set other value */
    public void setOtherValue(String key, Object value) {
        this.otherValues.put(key, value);
    }

    @Override
    public String toString() {
        return depositNumber + "_" + getErrorMessage();
    }
}
