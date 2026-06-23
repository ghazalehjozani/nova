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

class TradeLoanFacilityGuarantorAddedTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-20T10:15:30Z"), UTC);

    @Test
    void ofCarriesFullSnapshotAndDelta() {
        UUID facilityId = UUID.randomUUID();
        GuarantorParty existing = new GuarantorParty(
                "111",
                PartyType.REAL,
                new CustomerName("Ali", "Ahmadi", null),
                GuaranteePercentage.of(BigDecimal.valueOf(60)));
        GuarantorParty added = new GuarantorParty(
                "222",
                PartyType.LEGAL,
                new CustomerName(null, null, "Acme Co"),
                GuaranteePercentage.of(BigDecimal.valueOf(40)));

        TradeLoanFacilityGuarantorAdded event = TradeLoanFacilityGuarantorAdded.of(
                LoanFacilityId.of(facilityId), List.of(added), List.of(existing, added), FIXED_CLOCK);

        assertThat(event.eventType()).isEqualTo("TRADE_LOAN_FACILITY_GUARANTOR_ADDED");
        assertThat(event.aggregateId()).isEqualTo(facilityId);
        assertThat(event.createdAt()).isEqualTo(Instant.parse("2026-06-20T10:15:30Z"));
        assertThat(event.eventId()).isNotNull();

        assertThat(event.parties()).hasSize(2);
        assertThat(event.added()).hasSize(1);

        GuarantorPayload delta = event.added().get(0);
        assertThat(delta.customerNumber()).isEqualTo("222");
        assertThat(delta.partyType()).isEqualTo("LEGAL");
        assertThat(delta.customerName()).isEqualTo("Acme Co");
        assertThat(delta.guaranteePercent()).isEqualTo("40");

        assertThat(event.parties())
                .extracting(GuarantorPayload::customerNumber)
                .containsExactlyInAnyOrder("111", "222");
    }
}
