package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.CustomerInfoResultDTO")
public class CustomerInfoResponse extends FcbBaseResponse {

    @XStreamAlias("customerNumber")
    private Long customerNumber;

    @XStreamAlias("nationalCode")
    private String nationalCode;

    @XStreamAlias("firstName")
    private String firstName;

    @XStreamAlias("lastName")
    private String lastName;

    @XStreamAlias("title")
    private String title;

    @XStreamAlias("real")
    private Boolean real;

    @XStreamAlias("isInBlackList")
    private Boolean isInBlackList;

    @XStreamAlias("isIncapable")
    private Boolean isIncapable;

    @XStreamAlias("isInGrayList")
    private Boolean isInGrayList;

    // Additional fields that might be useful
    @XStreamAlias("fatherNameOrCompanyType")
    private String fatherNameOrCompanyType;

    @XStreamAlias("certificateNumberOrRegisterationNumber")
    private String certificateNumberOrRegisterationNumber;

    @XStreamAlias("birthDateOrRegisterDate")
    private String birthDateOrRegisterDate;

    @XStreamAlias("economicalCode")
    private String economicalCode;

    @XStreamAlias("institution")
    private String institution;

    @XStreamAlias("contactNumber")
    private String contactNumber;

    @XStreamAlias("mobileNumber")
    private String mobileNumber;

    @XStreamAlias("enable")
    private Boolean enable;

    @XStreamAlias("liveStatus")
    private Boolean liveStatus;

    @XStreamAlias("activityStatus")
    private String activityStatus;

    @XStreamAlias("shahabServiceCode")
    private String shahabServiceCode;

    @XStreamAlias("shahabServiceStatus")
    private String shahabServiceStatus;
}
