package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import java.math.BigDecimal;
import java.time.Period;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;

public record SanctionDetails(
        String sanctionSerialValue,
        SanctionType sanctionType,
        BigDecimal approvedAmount,
        CurrencyType currency,
        Period gracePeriod,
        Integer installmentCount,
        Period loanDuration,
        DisbursementMethod disbursementMethod,
        String lifeInsuranceId,
        String collateralSerial,
        String revocationReason) {}
