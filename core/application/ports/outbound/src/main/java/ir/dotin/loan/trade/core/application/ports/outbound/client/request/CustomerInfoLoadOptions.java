package ir.dotin.loan.trade.core.application.ports.outbound.client.request;

public record CustomerInfoLoadOptions(
        String sequenceCode,
        String subsystem,
        boolean includeCapability,
        boolean includeBlackList,
        boolean includeBaseInfo,
        boolean includeGrayList) {

    public static CustomerInfoLoadOptions allIncluded() {
        return new CustomerInfoLoadOptions(
                "GRANT_LOAN_RESTRICTION", "LOAN_BLACKLIST_RESTRICTION", true, true, true, true);
    }

    public static CustomerInfoLoadOptions baseInfoOnly() {
        return new CustomerInfoLoadOptions(
                "GRANT_LOAN_RESTRICTION", "LOAN_BLACKLIST_RESTRICTION", false, false, true, false);
    }

    public static CustomerInfoLoadOptions restrictionsOnly() {
        return new CustomerInfoLoadOptions(
                "GRANT_LOAN_RESTRICTION", "LOAN_BLACKLIST_RESTRICTION", true, true, false, true);
    }

    public static CustomerInfoLoadOptions custom(
            String sequenceCode,
            String subsystem,
            boolean includeCapability,
            boolean includeBlackList,
            boolean includeBaseInfo,
            boolean includeGrayList) {
        return new CustomerInfoLoadOptions(
                sequenceCode, subsystem, includeCapability, includeBlackList, includeBaseInfo, includeGrayList);
    }
}
