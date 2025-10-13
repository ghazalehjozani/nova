package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Data;

@Data
public class ElectronicBillCustomerDTO {

    @XStreamAlias("customerNumber")
    private String customerNumber;

    @XStreamAlias("shahabCode")
    private String shahabCode;

    @XStreamAlias("nationalCode")
    private String nationalCode;

    @XStreamAlias("customerType")
    private String customerType;

    @XStreamAlias("certificateNumber")
    private String certificateNumber;

    @XStreamAlias("birthDate")
    private String birthDate;

    @XStreamAlias("registrationDate")
    private String registrationDate;

    @XStreamAlias("birthCityCode")
    private String birthCityCode;

    @XStreamAlias("registrationCityCode")
    private String registrationCityCode;

    @XStreamAlias("phoneNumber")
    private String phoneNumber;

    @XStreamAlias("postalCode")
    private String postalCode;

    @XStreamAlias("customerGenderType")
    private String customerGenderType;

    @XStreamAlias("name")
    private String name;

    @XStreamAlias("address")
    private String address;
}
