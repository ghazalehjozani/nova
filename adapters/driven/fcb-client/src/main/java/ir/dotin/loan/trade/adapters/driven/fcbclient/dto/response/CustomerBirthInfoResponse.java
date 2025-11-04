package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.CustomerInfoResultDTO")
public class CustomerBirthInfoResponse extends FcbBaseResponse {

    @XStreamAlias("customerNumber")
    private Long customerNumber;

    @XStreamAlias("age")
    private Integer age;

    @XStreamAlias("growthOrder")
    private Boolean growthOrder;

    @XStreamAlias("isUnderEighteenYearsOld")
    private Boolean isUnderEighteenYearsOld;

    @XStreamAlias("birthDateOrRegisterDate")
    private String birthDate;

    @XStreamAlias("checkGrowthAge")
    private Boolean checkGrowthAge;
}
