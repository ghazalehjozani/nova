package ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments;

public class PenaltyPolicyDocument {

    private Double penaltyRate;

    private Double deferralInterestRate;

    private String penaltyFormula;

    private String penaltyPaymentType;

    public PenaltyPolicyDocument() {
    }

    public PenaltyPolicyDocument(Double penaltyRate, Double deferralInterestRate,
                                 String penaltyFormula, String penaltyPaymentType) {
        this.penaltyRate = penaltyRate;
        this.deferralInterestRate = deferralInterestRate;
        this.penaltyFormula = penaltyFormula;
        this.penaltyPaymentType = penaltyPaymentType;
    }

    public Double getPenaltyRate() {
        return penaltyRate;
    }

    public void setPenaltyRate(Double penaltyRate) {
        this.penaltyRate = penaltyRate;
    }

    public Double getDeferralInterestRate() {
        return deferralInterestRate;
    }

    public void setDeferralInterestRate(Double deferralInterestRate) {
        this.deferralInterestRate = deferralInterestRate;
    }

    public String getPenaltyFormula() {
        return penaltyFormula;
    }

    public void setPenaltyFormula(String penaltyFormula) {
        this.penaltyFormula = penaltyFormula;
    }

    public String getPenaltyPaymentType() {
        return penaltyPaymentType;
    }

    public void setPenaltyPaymentType(String penaltyPaymentType) {
        this.penaltyPaymentType = penaltyPaymentType;
    }
}
