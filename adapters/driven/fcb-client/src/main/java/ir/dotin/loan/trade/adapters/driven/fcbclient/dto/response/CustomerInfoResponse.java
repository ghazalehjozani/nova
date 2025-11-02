package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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

    // Additional fields
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

    // Complex nested objects - FCB may return these but we ignore most of them
    @XStreamImplicit(itemFieldName = "addresses")
    private List<Object> addresses; // Ignore nested addresses structure

    @XStreamImplicit(itemFieldName = "jobs")
    private List<Object> jobs; // Ignore nested jobs

    @XStreamImplicit(itemFieldName = "phones")
    private List<Object> phones; // Ignore nested phones

    @XStreamImplicit(itemFieldName = "customerActivities")
    private List<Object> customerActivities; // Ignore nested activities

    // More fields from actual FCB response that we want to capture
    @XStreamAlias("sex")
    private String sex;

    @XStreamAlias("nationalityCode")
    private String nationalityCode;

    @XStreamAlias("birthLocation")
    private String birthLocation;

    @XStreamAlias("birthLocationCode")
    private String birthLocationCode;

    @XStreamAlias("isPolitical")
    private Boolean isPolitical;

    @XStreamAlias("verifiedCellPhoneNumber")
    private Boolean verifiedCellPhoneNumber;

    @XStreamAlias("customerGroup")
    private String customerGroup;

    @XStreamAlias("levelStatus")
    private String levelStatus;

    @XStreamAlias("zonActivityTypeTitle")
    private String zoneActivityTypeTitle;

    @XStreamAlias("ssoId")
    private Integer ssoId;
}
