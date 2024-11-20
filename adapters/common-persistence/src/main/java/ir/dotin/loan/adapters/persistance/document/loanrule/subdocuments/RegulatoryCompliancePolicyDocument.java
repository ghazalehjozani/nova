package ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments;

public class RegulatoryCompliancePolicyDocument {

    private Integer overDuePeriod;

    private Integer deferralPeriod;

    private Integer suspiciousPeriod;

    public RegulatoryCompliancePolicyDocument() {
    }

    public RegulatoryCompliancePolicyDocument(Integer overDuePeriod, Integer deferralPeriod,
                                              Integer suspiciousPeriod) {
        this.overDuePeriod = overDuePeriod;
        this.deferralPeriod = deferralPeriod;
        this.suspiciousPeriod = suspiciousPeriod;
    }

    public Integer getOverDuePeriod() {
        return overDuePeriod;
    }

    public void setOverDuePeriod(Integer overDuePeriod) {
        this.overDuePeriod = overDuePeriod;
    }

    public Integer getDeferralPeriod() {
        return deferralPeriod;
    }

    public void setDeferralPeriod(Integer deferralPeriod) {
        this.deferralPeriod = deferralPeriod;
    }

    public Integer getSuspiciousPeriod() {
        return suspiciousPeriod;
    }

    public void setSuspiciousPeriod(Integer suspiciousPeriod) {
        this.suspiciousPeriod = suspiciousPeriod;
    }
}
