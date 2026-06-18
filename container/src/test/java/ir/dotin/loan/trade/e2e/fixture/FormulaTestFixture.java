package ir.dotin.loan.trade.e2e.fixture;

import java.util.Set;

import org.springframework.boot.test.context.TestComponent;

import ir.dotin.platform.formula.infrastructure.persistence.entity.FormulaEntity;
import ir.dotin.platform.formula.infrastructure.persistence.repository.FormulaJpaRepository;
import ir.dotin.platform.pangaea.commons.domain.entity.Identity;

import lombok.RequiredArgsConstructor;

@TestComponent
@RequiredArgsConstructor
public class FormulaTestFixture {

    private final FormulaJpaRepository formulaRepository;

    /**
     * Creates all standard formula definitions needed by loan arrangement policies. Must be called before creating
     * arrangements that reference these formulas.
     */
    public void createDefaultFormulas() {
        createFormulaIfAbsent(
                "10000",
                "approvedAmount * (interestRate / 100) * (durationMonths / 12)",
                "Interest/grace period calculation formula",
                Set.of("approvedAmount", "interestRate", "durationMonths"));

        createFormulaIfAbsent(
                "2000",
                "approvedAmount / installmentCount",
                "Installment/refund interest calculation formula",
                Set.of("approvedAmount", "installmentCount"));

        createFormulaIfAbsent(
                "50000",
                "overdueAmount * (penaltyRate / 100) * (overdueDays / 365)",
                "Penalty calculation formula",
                Set.of("overdueAmount", "penaltyRate", "overdueDays"));

        createFormulaIfAbsent(
                "1000",
                "totalInterest / installmentCount",
                "Interest component calculation formula",
                Set.of("totalInterest", "installmentCount"));
    }

    // expression-kit 2026.6.10: FormulaEntity now carries a UUID v7 surrogate id with `code` as the business key
    // (lookup via findByCode/existsByCode). Construct with a generated v7 id; keep the old numeric strings as the code.
    private void createFormulaIfAbsent(String code, String expression, String description, Set<String> variables) {
        if (!formulaRepository.existsByCode(code)) {
            FormulaEntity entity = new FormulaEntity(Identity.generateV7(), code, expression);
            entity.setDescription(description);
            entity.setVariables(variables);
            formulaRepository.save(entity);
        }
    }
}
