package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component;

import java.util.Map;
import java.util.Optional;

import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralValidation;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public record CollateralValidationContext(
        TradeLoanFacility facility,
        TradeLoanArrangement arrangement,
        Optional<InstallmentSchedule> schedule,
        Money requiredCollateralAmount,
        Map<CollateralSerial, CollateralDetails> collateralDetailsMap,
        CollateralValidation collateralValidation) {}
