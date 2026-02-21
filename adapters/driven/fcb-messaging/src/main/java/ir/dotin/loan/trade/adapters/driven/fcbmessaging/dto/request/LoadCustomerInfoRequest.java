package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadCustomerInfoRequest extends FcbKafkaBaseRequest {

    private final String customerNumber;
    private final String sequenceCode;
    private final String subsystem;
    private final boolean includeCapability;
    private final boolean includeBlackList;
    private final boolean includeBaseInfo;
    private final boolean includeGrayList;

    public LoadCustomerInfoRequest(
            String customerNumber,
            String sequenceCode,
            String subsystem,
            boolean includeCapability,
            boolean includeBlackList,
            boolean includeBaseInfo,
            boolean includeGrayList) {
        super("load-customer-info");
        this.customerNumber = customerNumber;
        this.sequenceCode = sequenceCode;
        this.subsystem = subsystem;
        this.includeCapability = includeCapability;
        this.includeBlackList = includeBlackList;
        this.includeBaseInfo = includeBaseInfo;
        this.includeGrayList = includeGrayList;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getSequenceCode() {
        return sequenceCode;
    }

    public String getSubsystem() {
        return subsystem;
    }

    public boolean isIncludeCapability() {
        return includeCapability;
    }

    public boolean isIncludeBlackList() {
        return includeBlackList;
    }

    public boolean isIncludeBaseInfo() {
        return includeBaseInfo;
    }

    public boolean isIncludeGrayList() {
        return includeGrayList;
    }
}
