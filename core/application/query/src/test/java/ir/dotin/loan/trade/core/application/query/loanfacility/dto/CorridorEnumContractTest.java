package ir.dotin.loan.trade.core.application.query.loanfacility.dto;

import org.junit.jupiter.api.Test;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.pangaea.commons.core.i18n.LocalizedEnum;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the Nova → FCB corridor wire contract: every enum on {@link TradeFacilityQueryDto} that FCB reads back as a
 * {@code {code,label}} object must stay a {@code LocalizedEnum}. The pangaea {@code WireSerializationModule} renders a
 * {@code LocalizedEnum} as {@code {code,label}} and every other enum as a bare string; if one of these loses the marker
 * it silently turns into a bare string. FCB's {@code NovaCodeLabel} tolerates both shapes, but keeping these
 * {@code LocalizedEnum} preserves the human-facing label across the corridor and keeps the wire contract stable.
 */
class CorridorEnumContractTest {

    @Test
    void facilityCorridorEnumsStayLocalized() {
        assertThat(LocalizedEnum.class)
                .as("enums consumed by the FCB corridor as {code,label} must remain LocalizedEnum")
                .isAssignableFrom(
                        ApplicantChannel.class,
                        DisburseDestinationType.class,
                        DisbursementMethod.class,
                        PartyType.class,
                        PartyRole.class,
                        FacilityStatus.class,
                        TransactionStatus.class);
    }
}
