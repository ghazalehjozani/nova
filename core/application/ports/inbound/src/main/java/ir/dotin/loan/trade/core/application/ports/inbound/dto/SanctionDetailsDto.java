package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import java.math.BigDecimal;
import java.time.Period;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;

import lombok.Builder;

/**
 * Transport-neutral carrier for the FCB-resolved sanction details, produced by the tx-free manual-approval pre-flight
 * query ({@code PrepareFacilityApprovalQuery}) and threaded back into
 * {@link ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand} so the lean transactional
 * command can rebuild the outbound {@code SanctionDetails} without re-issuing the FCB read.
 *
 * <p>Carries every field of the outbound {@code SanctionDetails} but uses only inbound-safe types — the inbound module
 * may not import the outbound {@code SanctionDetails} nor the platform {@code CurrencyType} value object. Accordingly:
 *
 * <ul>
 *   <li>{@code sanctionType} and {@code disbursementMethod} are base-loan ENUMS (allowed in inbound).
 *   <li>{@code currencyCode} carries the {@code CurrencyType} ISO code as a {@link String} (the platform
 *       {@code CurrencyType} VO is reconstructed in the service layer via {@code CurrencyType.valueOf(code)}).
 *   <li>{@code confirmTypePersonCode} carries the {@code ConfirmType} value as a {@link String} — {@code ConfirmType}
 *       is a base-loan record value object (NOT an enum), so it is threaded as its {@code personCode} string and
 *       reconstructed in the service layer via {@code ConfirmType.of(personCode)}.
 *   <li>{@code gracePeriod}/{@code loanDuration} are JDK {@link Period} (allowed in inbound).
 * </ul>
 */
@Builder
public record SanctionDetailsDto(
        String sanctionSerialValue,
        SanctionType sanctionType,
        BigDecimal approvedAmount,
        String currencyCode,
        Period gracePeriod,
        Integer installmentCount,
        Period loanDuration,
        DisbursementMethod disbursementMethod,
        @Nullable String lifeInsuranceId,
        @Nullable String collateralSerial,
        @Nullable String revocationReason,
        String confirmTypePersonCode) {}
