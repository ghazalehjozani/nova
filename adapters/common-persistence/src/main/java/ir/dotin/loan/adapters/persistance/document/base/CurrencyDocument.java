package ir.dotin.loan.adapters.persistance.document.base;

public class CurrencyDocument {

    private String code;

    private String name;

    public CurrencyDocument() {
    }

    public CurrencyDocument(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
