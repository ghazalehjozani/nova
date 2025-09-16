package ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ConfirmTypeEmb {

    @Column(name = "confirm_person_code", length = 50)
    private String personCode;

    @Column(name = "confirm_person_name", length = 100)
    private String personName;

    public ConfirmTypeEmb() {}

    public ConfirmTypeEmb(String personCode, String personName) {
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
