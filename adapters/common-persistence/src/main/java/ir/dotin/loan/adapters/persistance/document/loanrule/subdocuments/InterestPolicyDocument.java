package ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments;

import ir.dotin.loan.adapters.persistance.document.base.RateRangeDocument;

public class InterestPolicyDocument {

    private Double baseInterestRate;

    private RateRangeDocument preferentialRangeRate;

    private String interestFormula;

    private String refundInterestFormula;

    private boolean dailyInterest;

    public InterestPolicyDocument() {
    }

    public InterestPolicyDocument(Double baseInterestRate, RateRangeDocument preferentialRangeRate,
                                  String interestFormula, String refundInterestFormula,
                                  boolean dailyInterest) {
        this.baseInterestRate = baseInterestRate;
        this.preferentialRangeRate = preferentialRangeRate;
        this.interestFormula = interestFormula;
        this.refundInterestFormula = refundInterestFormula;
        this.dailyInterest = dailyInterest;
    }

    public Double getBaseInterestRate() {
        return baseInterestRate;
    }

    public void setBaseInterestRate(Double baseInterestRate) {
        this.baseInterestRate = baseInterestRate;
    }

    public RateRangeDocument getPreferentialRangeRate() {
        return preferentialRangeRate;
    }

    public void setPreferentialRangeRate(
            RateRangeDocument preferentialRangeRate) {
        this.preferentialRangeRate = preferentialRangeRate;
    }

    public String getInterestFormula() {
        return interestFormula;
    }

    public void setInterestFormula(String interestFormula) {
        this.interestFormula = interestFormula;
    }

    public String getRefundInterestFormula() {
        return refundInterestFormula;
    }

    public void setRefundInterestFormula(String refundInterestFormula) {
        this.refundInterestFormula = refundInterestFormula;
    }

    public boolean isDailyInterest() {
        return dailyInterest;
    }

    public void setDailyInterest(boolean dailyInterest) {
        this.dailyInterest = dailyInterest;
    }
}
