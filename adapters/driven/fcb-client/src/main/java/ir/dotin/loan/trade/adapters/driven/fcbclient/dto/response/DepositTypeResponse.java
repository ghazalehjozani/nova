package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositTypeResponse {

    @XStreamAlias("id")
    private Long id;

    @XStreamAlias("code")
    private String code;

    @XStreamAlias("title")
    private String title;

    @XStreamAlias("description")
    private String description;
}
