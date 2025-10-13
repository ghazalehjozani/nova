package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("ir.dotin.lc.dto.ilccredit.bill.ElectronicBillIssueDocumentVO")
public class IssueDocumentResponse extends FcbBaseResponse {

    @XStreamAlias("transaction")
    private String transaction;
}
