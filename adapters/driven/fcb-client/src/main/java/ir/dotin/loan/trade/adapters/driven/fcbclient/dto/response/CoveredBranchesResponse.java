package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoveredBranchesResponse extends FcbBaseResponse {

    @XStreamImplicit(itemFieldName = "BranchDTO")
    private List<BranchResponse> branches;
}
