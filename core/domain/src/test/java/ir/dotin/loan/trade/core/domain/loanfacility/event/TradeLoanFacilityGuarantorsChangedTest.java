package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuaranteePercentage;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

class TradeLoanFacilityGuarantorsChangedTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-20T10:15:30Z"), UTC);

    @Test
    void ofMapsGuarantorPartiesToWirePayload() {
        UUID facilityId = UUID.randomUUID();
        GuarantorParty real = new GuarantorParty(
                "111",
                PartyType.REAL,
                new CustomerName("Ali", "Ahmadi", null),
                GuaranteePercentage.of(BigDecimal.valueOf(60)));
        GuarantorParty legal = new GuarantorParty(
                "222",
                PartyType.LEGAL,
                new CustomerName(null, null, "Acme Co"),
                GuaranteePercentage.of(BigDecimal.valueOf(40)));

        TradeLoanFacilityGuarantorsChanged event =
                TradeLoanFacilityGuarantorsChanged.of(LoanFacilityId.of(facilityId), List.of(real, legal), FIXED_CLOCK);

        assertThat(event.eventType()).isEqualTo("TRADE_LOAN_FACILITY_GUARANTORS_CHANGED");
        assertThat(event.aggregateId()).isEqualTo(facilityId);
        assertThat(event.createdAt()).isEqualTo(Instant.parse("2026-06-20T10:15:30Z"));
        assertThat(event.eventId()).isNotNull();

        assertThat(event.parties()).hasSize(2);

        TradeLoanFacilityGuarantorsChanged.GuarantorPayload first =
                event.parties().get(0);
        assertThat(first.customerNumber()).isEqualTo("111");
        assertThat(first.partyType()).isEqualTo("REAL");
        assertThat(first.customerName()).isEqualTo("Ali Ahmadi");
        assertThat(first.guaranteePercent()).isEqualTo("60");

        TradeLoanFacilityGuarantorsChanged.GuarantorPayload second =
                event.parties().get(1);
        assertThat(second.customerNumber()).isEqualTo("222");
        assertThat(second.partyType()).isEqualTo("LEGAL");
        assertThat(second.customerName()).isEqualTo("Acme Co");
        assertThat(second.guaranteePercent()).isEqualTo("40");
    }

    @Test
    void ofProducesEmptyPayloadForNoGuarantors() {
        TradeLoanFacilityGuarantorsChanged event =
                TradeLoanFacilityGuarantorsChanged.of(LoanFacilityId.of(UUID.randomUUID()), List.of(), FIXED_CLOCK);

        assertThat(event.parties()).isEmpty();
        assertThat(event.eventType()).isEqualTo("TRADE_LOAN_FACILITY_GUARANTORS_CHANGED");
    }
}
