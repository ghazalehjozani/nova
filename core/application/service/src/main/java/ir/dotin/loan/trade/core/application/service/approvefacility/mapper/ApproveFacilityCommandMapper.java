package ir.dotin.loan.trade.core.application.service.approvefacility.mapper;

import jakarta.annotation.Nullable;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.GracePeriod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.RevocationReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LifeInsuranceId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;

@Mapper(config = BaseMapperConfig.class)
public interface ApproveFacilityCommandMapper {

    @Mapping(target = "disbursementSchedule", ignore = true)
    TradeSanctionedLoan.Builder toBuilder(ApproveFacilityCommand command);

    SanctionedLoanId map(ApproveFacilityCommand.SanctionedLoanIdDto dto);

    SanctionSerial map(ApproveFacilityCommand.SanctionSerialDto dto);

    Money map(ApproveFacilityCommand.MoneyDto dto);

    GracePeriod map(ApproveFacilityCommand.GracePeriodDto dto);

    InstallmentCount map(ApproveFacilityCommand.InstallmentCountDto dto);

    LoanDuration map(ApproveFacilityCommand.LoanDurationDto dto);

    @Nullable
    LifeInsuranceId map(@Nullable ApproveFacilityCommand.LifeInsuranceIdDto dto);

    @Nullable
    CollateralSerial map(@Nullable ApproveFacilityCommand.CollateralSerialDto dto);

    @Nullable
    RevocationReason map(@Nullable ApproveFacilityCommand.RevocationReasonDto dto);
}
