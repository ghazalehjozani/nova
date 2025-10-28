package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferMoneyCurrencyResponse {

    @XStreamAlias("id")
    private Long id;

    @XStreamAlias("code")
    private String code;

    @XStreamAlias("name")
    private String name;

    @XStreamAlias("symbol")
    private String symbol;

    @XStreamAlias("swiftCode")
    private String swiftCode;
}
