package ir.dotin.loan.trade.adapters.driven.persistance.entity;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import ir.dotin.loan.trade.adapters.driven.persistance.AbstractEntity;

@Entity
@Table(name = "collateral_type")
public class CollateralTypeEntity extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "percent", precision = 5, scale = 2)
    private BigDecimal percent;

    @Column(name = "minimum_value", precision = 15, scale = 2)
    private BigDecimal minimumValue;

    @Column(name = "maximum_value", precision = 15, scale = 2)
    private BigDecimal maximumValue;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "valuation_factor", precision = 5, scale = 2)
    private BigDecimal valuationFactor;

    public CollateralTypeEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public void setPercent(BigDecimal percent) {
        this.percent = percent;
    }

    public BigDecimal getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(BigDecimal minimumValue) {
        this.minimumValue = minimumValue;
    }

    public BigDecimal getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(BigDecimal maximumValue) {
        this.maximumValue = maximumValue;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean required) {
        isRequired = required;
    }

    public BigDecimal getValuationFactor() {
        return valuationFactor;
    }

    public void setValuationFactor(BigDecimal valuationFactor) {
        this.valuationFactor = valuationFactor;
    }
}
