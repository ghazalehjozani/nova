package ir.dotin.loan.trade.e2e.orchestrator;

import java.util.UUID;

import org.springframework.boot.test.context.TestComponent;

import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.e2e.fixture.FormulaTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanArrangementTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanFacilityTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanFacilityTestFixture.DisbursedFacilityResult;
import ir.dotin.loan.trade.e2e.fixture.LoanTypeTestFixture;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@TestComponent
@RequiredArgsConstructor
public class PrerequisiteOrchestrator {

    private final FormulaTestFixture formulaFixture;
    private final LoanArrangementTestFixture arrangementFixture;
    private final LoanTypeTestFixture loanTypeFixture;
    private final LoanFacilityTestFixture facilityFixture;

    private volatile boolean formulasCreated;

    public record MinimalChain(TradeLoanArrangementEntity arrangement, TradeLoanTypeEntity loanType) {}

    public record FullChain(MinimalChain base, DisbursedFacilityResult facility) {}

    public void ensureFormulas() {
        if (!formulasCreated) {
            formulaFixture.createDefaultFormulas();
            formulasCreated = true;
        }
    }

    public MinimalChain createMinimalChain() {
        ensureFormulas();
        TradeLoanArrangementEntity arrangement = arrangementFixture.createDefaultArrangement();
        // persisted entity: generated id is non-null by contract after save
        TradeLoanTypeEntity loanType =
                loanTypeFixture.createDefaultLoanType(requireNonNull(arrangement.getId(), "arrangement id after save"));
        return new MinimalChain(arrangement, loanType);
    }

    public MinimalChain createMinimalChain(String arrangementCode, String loanTypeCode) {
        ensureFormulas();
        TradeLoanArrangementEntity arrangement = arrangementFixture.createArrangement(arrangementCode, e -> {});
        // persisted entity: generated id is non-null by contract after save
        TradeLoanTypeEntity loanType = loanTypeFixture.createLoanType(
                loanTypeCode, requireNonNull(arrangement.getId(), "arrangement id after save"), e -> {});
        return new MinimalChain(arrangement, loanType);
    }

    public FullChain createFullChain() {
        MinimalChain base = createMinimalChain();
        // persisted fixtures: id and code are non-null by contract after save
        UUID loanTypeId = requireNonNull(base.loanType().getId(), "loan type id after save");
        String loanTypeCode = requireNonNull(
                requireNonNull(base.loanType().getCode(), "loan type code after save")
                        .getValue(),
                "loan type code value after save");
        UUID arrangementId = requireNonNull(base.arrangement().getId(), "arrangement id after save");
        DisbursedFacilityResult facility =
                facilityFixture.createDisbursedFacilityForCollection(loanTypeId, loanTypeCode, arrangementId);
        return new FullChain(base, facility);
    }
}
