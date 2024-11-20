package ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments;

public class ConfirmTypeDocument {

    private String personCode;
    private String personName;

    public ConfirmTypeDocument() {
    }

    public ConfirmTypeDocument(String personCode, String personName) {
        this.personCode = personCode;
        this.personName = personName;
    }

    public String getPersonCode() {
        return personCode;
    }

    public void setPersonCode(String personCode) {
        this.personCode = personCode;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

}
