package ir.dotin.loan.trade.e2e.fixture;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.boot.test.context.TestComponent;

import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.EconomicSectorCurrencyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.LoanTypeCodeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RelationTypeLoanTopicEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.TitleEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeJpaRepository;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.RequiredArgsConstructor;

@TestComponent
@RequiredArgsConstructor
public class LoanTypeTestFixture {

    private final TradeLoanTypeJpaRepository repository;

    public TradeLoanTypeEntity createDefaultLoanType(UUID arrangementId) {
        return createLoanType("E2E-TYPE-" + UUID.randomUUID().toString().substring(0, 8), arrangementId, e -> {});
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
        entity.setRelationTypeLoanTopics(buildDefaultRelationTypeLoanTopics());

        customizer.accept(entity);

        return repository.save(entity);
    }

    private Set<RelationTypeLoanTopicEmb> buildDefaultRelationTypeLoanTopics() {
        Set<String> sectors = Set.of("2-1");
        return Set.of(
                new RelationTypeLoanTopicEmb(TradeRelationType.BANK_COMMITMENTS, "تعهدات بانک", "835", sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.BANK_COMMITMENTS_CONTRA, "طرف تعهدات بانک", "883", sectors),
                new RelationTypeLoanTopicEmb(TradeRelationType.PRINCIPAL, "اصلی", "1053", sectors),
                new RelationTypeLoanTopicEmb(TradeRelationType.FUTURE_INTEREST, "سود سالهای آینده", "20140", sectors),
                new RelationTypeLoanTopicEmb(TradeRelationType.DISCOUNT, "تخفیف", "1837", sectors),
                new RelationTypeLoanTopicEmb(TradeRelationType.RECEIVED_INTEREST, "سود دريافتي", "2288", sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.ACCRUED_DEFERRED_INTEREST, "سود معوق تعهدي", "1053", sectors),
                new RelationTypeLoanTopicEmb(TradeRelationType.RECEIVABLES_DOUBTFUL, "مطالبات معوق", "1428", sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.RECEIVABLES_WRITTEN_OFF, "مطالبات سوخت شده", "879", sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.RECEIVABLES_SUBSTANDARD, "مطالبات مشکوک الوصول", "1264", sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.RECEIVABLES_PAST_DUE, "مطالبات بعد از سررسيد", "1470", sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.RECEIVABLES_ACCRUED_DEFERRED_INTEREST,
                        "سود معوق تعهدي مطالبات",
                        "1530",
                        sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.RECEIVABLES_ACCRUED_PENALTY, "جريمه تعهدي مطالبات", "1053", sectors),
                new RelationTypeLoanTopicEmb(TradeRelationType.ACCRUED_PENALTY, "جريمه تعهدي", "1837", sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.RECEIVABLES_DEFERRED_INTEREST, "سود معوق مطالبات", "200718", sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.INTEREST_SHORTFALL_PROVISION, "تامين کسری سود", "880", sectors),
                new RelationTypeLoanTopicEmb(TradeRelationType.PENALTY, "جریمه", "23050", sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.RECEIVABLES_FUTURE_INTEREST, "سود سررسید آتی مطالبات", "23050", sectors),
                new RelationTypeLoanTopicEmb(TradeRelationType.RECEIVABLES_PENALTY, "جریمه مطالبات", "23050", sectors),
                new RelationTypeLoanTopicEmb(TradeRelationType.DEFERRED_INTEREST, "سود معوق", "23050", sectors),
                new RelationTypeLoanTopicEmb(TradeRelationType.ACCRUED_INTEREST, "سود تعهدی", "23050", sectors),
                new RelationTypeLoanTopicEmb(
                        TradeRelationType.RECEIVABLES_OVERDUE, "مطالبات سررسید گذشته", "23050", sectors));
    }
}
