package ir.dotin.loan.trade.e2e.fixture;

import java.util.Set;

import org.springframework.boot.test.context.TestComponent;

import ir.dotin.platform.formula.infrastructure.persistence.entity.FormulaEntity;
import ir.dotin.platform.formula.infrastructure.persistence.repository.FormulaJpaRepository;

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

    private void createFormulaIfAbsent(String id, String expression, String description, Set<String> variables) {
        if (formulaRepository.findById(id).isEmpty()) {
            FormulaEntity entity = new FormulaEntity(id, expression);
            entity.setDescription(description);
            entity.setVariables(variables);
            formulaRepository.save(entity);
        }
    }
}
