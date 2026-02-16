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
        TradeLoanTypeEntity loanType = loanTypeFixture.createDefaultLoanType(arrangement.getId());
        return new MinimalChain(arrangement, loanType);
    }

    public MinimalChain createMinimalChain(String arrangementCode, String loanTypeCode) {
        ensureFormulas();
        TradeLoanArrangementEntity arrangement = arrangementFixture.createArrangement(arrangementCode, e -> {});
        TradeLoanTypeEntity loanType = loanTypeFixture.createLoanType(loanTypeCode, arrangement.getId(), e -> {});
        return new MinimalChain(arrangement, loanType);
    }

    public FullChain createFullChain() {
        MinimalChain base = createMinimalChain();
        UUID loanTypeId = base.loanType().getId();
        String loanTypeCode = base.loanType().getCode().getValue();
        UUID arrangementId = base.arrangement().getId();
        DisbursedFacilityResult facility =
                facilityFixture.createDisbursedFacilityForCollection(loanTypeId, loanTypeCode, arrangementId);
        return new FullChain(base, facility);
    }
}
