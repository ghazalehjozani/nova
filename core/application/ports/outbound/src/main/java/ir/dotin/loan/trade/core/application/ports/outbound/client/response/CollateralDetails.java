package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import java.math.BigDecimal;

public record CollateralDetails(
        String serial,
        String customerNo,
        String assuranceTypeCode,
        String assuranceTypeName,
        BigDecimal guaranteeAmount,
        BigDecimal price,
        BigDecimal usedMortgagePrice,
        Integer guaranteeDuration,
        String guaranteeNumber,
        String guaranteeIssuer,
        String guaranteeBranchCode,
        String loanFileNumber,
        String branchCode,
        String currency,
        boolean active,
        boolean isEscrowed,
        boolean isReleaseAllowed,
        boolean isSpecial,
        String address) {}
