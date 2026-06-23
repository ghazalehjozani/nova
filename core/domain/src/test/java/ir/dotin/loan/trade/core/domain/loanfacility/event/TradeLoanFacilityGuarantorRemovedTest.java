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

class TradeLoanFacilityGuarantorRemovedTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-20T10:15:30Z"), UTC);

    @Test
    void ofCarriesResultingSnapshotAndRemovedCustomerNumber() {
        UUID facilityId = UUID.randomUUID();
        GuarantorParty remaining = new GuarantorParty(
                "111",
                PartyType.REAL,
                new CustomerName("Ali", "Ahmadi", null),
                GuaranteePercentage.of(BigDecimal.valueOf(100)));

        TradeLoanFacilityGuarantorRemoved event = TradeLoanFacilityGuarantorRemoved.of(
                LoanFacilityId.of(facilityId), "222", List.of(remaining), FIXED_CLOCK);

        assertThat(event.eventType()).isEqualTo("TRADE_LOAN_FACILITY_GUARANTOR_REMOVED");
        assertThat(event.aggregateId()).isEqualTo(facilityId);
        assertThat(event.createdAt()).isEqualTo(Instant.parse("2026-06-20T10:15:30Z"));
        assertThat(event.eventId()).isNotNull();
        assertThat(event.removedCustomerNumber()).isEqualTo("222");

        assertThat(event.parties()).hasSize(1);
        GuarantorPayload payload = event.parties().get(0);
        assertThat(payload.customerNumber()).isEqualTo("111");
        assertThat(payload.guaranteePercent()).isEqualTo("100");
    }

    @Test
    void ofProducesEmptySnapshotWhenLastGuarantorRemoved() {
        TradeLoanFacilityGuarantorRemoved event = TradeLoanFacilityGuarantorRemoved.of(
                LoanFacilityId.of(UUID.randomUUID()), "111", List.of(), FIXED_CLOCK);

        assertThat(event.parties()).isEmpty();
        assertThat(event.removedCustomerNumber()).isEqualTo("111");
        assertThat(event.eventType()).isEqualTo("TRADE_LOAN_FACILITY_GUARANTOR_REMOVED");
    }
}
