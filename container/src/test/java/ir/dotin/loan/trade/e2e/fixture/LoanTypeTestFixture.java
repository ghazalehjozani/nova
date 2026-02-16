package ir.dotin.loan.trade.e2e.fixture;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.boot.test.context.TestComponent;

import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.EconomicSectorCurrencyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.LoanTypeCodeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.TitleEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeJpaRepository;

import lombok.RequiredArgsConstructor;

@TestComponent
@RequiredArgsConstructor
public class LoanTypeTestFixture {

    private final TradeLoanTypeJpaRepository repository;

    public TradeLoanTypeEntity createDefaultLoanType(UUID arrangementId) {
        return createLoanType(
                "E2E-TYPE-" + UUID.randomUUID().toString().substring(0, 8),
                arrangementId,
                e -> {});
    }

    public TradeLoanTypeEntity createLoanType(
            String code, UUID arrangementId, Consumer<TradeLoanTypeEntity> customizer) {

        TradeLoanTypeEntity entity = new TradeLoanTypeEntity();
        entity.setId(UUID.randomUUID());

        LoanTypeCodeEmb codeEmb = new LoanTypeCodeEmb();
        codeEmb.setValue(code);
        entity.setCode(codeEmb);

        TitleEmb title = new TitleEmb();
        title.setValue("E2E Test Loan Type");
        entity.setTitle(title);

        entity.setGatewayType(GatewayType.DIGITAL_BANK);
        entity.setLoanApplicationAllowed(true);
        entity.setActive(true);
        entity.setDisable(false);

        Set<UUID> arrangementIds = new HashSet<>();
        arrangementIds.add(arrangementId);
        entity.setLoanArrangementIds(arrangementIds);

        EconomicSectorCurrencyEmb sectorCurrency = new EconomicSectorCurrencyEmb();
        sectorCurrency.setEconomicSectorCode("2-1");
        sectorCurrency.setCurrencyTypes(Set.of("IRR", "EUR"));
        entity.setEconomicSectorCurrencies(Set.of(sectorCurrency));

        entity.setIncomeIds(Set.of());
        entity.setRelationTypeLoanTopics(Set.of());

        customizer.accept(entity);

        return repository.save(entity);
    }
}
