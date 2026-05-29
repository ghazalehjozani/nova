package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import java.math.BigDecimal;
import java.time.Period;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;

public record SanctionDetails(
        String sanctionSerialValue,
        SanctionType sanctionType,
        BigDecimal approvedAmount,
        CurrencyType currency,
        Period gracePeriod,
        Integer installmentCount,
        Period loanDuration,
        DisbursementMethod disbursementMethod,
        @Nullable String lifeInsuranceId,
        @Nullable String collateralSerial,
        @Nullable String revocationReason,
        ConfirmType confirmType) {}
