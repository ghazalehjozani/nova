package ir.dotin.loan.trade.e2e.rest.loantype;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineLoanTypeRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineLoanTypeRequest.EconomicSectorCurrencyDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineLoanTypeRequest.RelationTypeLoanTopicDto;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator.MinimalChain;

import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.assertSuccess;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class DefineLoanTypeRestE2ETest extends AbstractRestE2E {

    private TradeLoanArrangementEntity arrangement;

    @BeforeAll
    void setupFixtures() {
        MinimalChain chain = prerequisiteOrchestrator.createMinimalChain();
        arrangement = chain.arrangement();
    }

    @Test
    void shouldDefineLoanTypeSuccessfully() {
        DefineLoanTypeRequest request =
                buildLoanTypeRequest("E2E-LT-" + UUID.randomUUID().toString().substring(0, 8), arrangement.getCode());

        ResponseEntity<String> response = postJson("/loan-types/define", request);

        assertSuccess(response, objectMapper);
    }

    @Test
    void shouldRejectLoanTypeWithInvalidArrangementCode() {
        DefineLoanTypeRequest request = buildLoanTypeRequest(
                "E2E-LT-" + UUID.randomUUID().toString().substring(0, 8), "NON_EXISTENT_ARRANGEMENT_CODE");

        ResponseEntity<String> response = postJson("/loan-types/define", request);

        assertThat(response.getStatusCode().is4xxClientError()
                        || response.getStatusCode().is5xxServerError())
                .as(
                        "Invalid arrangement code should be rejected, got: %s %s",
                        response.getStatusCode(), response.getBody())
                .isTrue();
    }

    private DefineLoanTypeRequest buildLoanTypeRequest(String code, String arrangementCode) {
        Set<String> sectors = Set.of("2-1");
        return new DefineLoanTypeRequest(
                code,
                "E2E Test Loan Type",
                GatewayType.DIGITAL_BANK,
                true,
                List.of(new EconomicSectorCurrencyDto("2-1", Set.of("IRR", "EUR"))),
                Set.of(arrangementCode),
                List.of(
                        new RelationTypeLoanTopicDto(TradeRelationType.BANK_COMMITMENTS, "تعهدات بانک", "835", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.BANK_COMMITMENTS_CONTRA, "طرف تعهدات بانک", "883", sectors),
                        new RelationTypeLoanTopicDto(TradeRelationType.PRINCIPAL, "اصلی", "1053", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.FUTURE_INTEREST, "سود سالهای آینده", "20140", sectors),
                        new RelationTypeLoanTopicDto(TradeRelationType.DISCOUNT, "تخفیف", "1837", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVED_INTEREST, "سود دريافتي", "2288", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.ACCRUED_DEFERRED_INTEREST, "سود معوق تعهدي", "1053", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVABLES_DOUBTFUL, "مطالبات معوق", "1428", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVABLES_WRITTEN_OFF, "مطالبات سوخت شده", "879", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVABLES_SUBSTANDARD, "مطالبات مشکوک الوصول", "1264", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVABLES_PAST_DUE, "مطالبات بعد از سررسيد", "1470", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVABLES_ACCRUED_DEFERRED_INTEREST,
                                "سود معوق تعهدي مطالبات",
                                "1530",
                                sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVABLES_ACCRUED_PENALTY, "جريمه تعهدي مطالبات", "1053", sectors),
                        new RelationTypeLoanTopicDto(TradeRelationType.ACCRUED_PENALTY, "جريمه تعهدي", "1837", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVABLES_DEFERRED_INTEREST, "سود معوق مطالبات", "200718", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.INTEREST_SHORTFALL_PROVISION, "تامين کسری سود", "880", sectors),
                        new RelationTypeLoanTopicDto(TradeRelationType.PENALTY, "جریمه", "23050", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVABLES_FUTURE_INTEREST,
                                "سود سررسید آتی مطالبات",
                                "23050",
                                sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVABLES_PENALTY, "جریمه مطالبات", "23050", sectors),
                        new RelationTypeLoanTopicDto(TradeRelationType.DEFERRED_INTEREST, "سود معوق", "23050", sectors),
                        new RelationTypeLoanTopicDto(TradeRelationType.ACCRUED_INTEREST, "سود تعهدی", "23050", sectors),
                        new RelationTypeLoanTopicDto(
                                TradeRelationType.RECEIVABLES_OVERDUE, "مطالبات سررسید گذشته", "23050", sectors)),
                Map.of());
    }
}
