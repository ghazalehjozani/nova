package ir.dotin.loan.adapters.persistance.document.loanrule.subdocuments;

import java.util.Set;

public class CollateralPolicyDocument {

    private Set<CollateralTypeDocument> collateralTypes;

    private Integer totalPercent;

    public CollateralPolicyDocument() {
    }

    public CollateralPolicyDocument(Set<CollateralTypeDocument> collateralTypes,
                                    Integer totalPercent) {
        this.collateralTypes = collateralTypes;
        this.totalPercent = totalPercent;
    }

    public Set<CollateralTypeDocument> getCollateralTypes() {
        return collateralTypes;
    }

    public void setCollateralTypes(Set<CollateralTypeDocument> collateralTypes) {
        this.collateralTypes = collateralTypes;
    }

    public Integer getTotalPercent() {
        return totalPercent;
    }

    public void setTotalPercent(Integer totalPercent) {
        this.totalPercent = totalPercent;
    }

}
