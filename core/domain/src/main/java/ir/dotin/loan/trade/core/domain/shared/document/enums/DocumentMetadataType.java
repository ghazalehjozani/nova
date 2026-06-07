package ir.dotin.loan.trade.core.domain.shared.document.enums;

public enum DocumentMetadataType {
    DISBURSEMENT("LOAN_GRANT_LOAN"),
    ISSUE_CONTRACT("LOAN_ISSUE_LOAN_CONTRACT");

    private final String code;

    DocumentMetadataType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
