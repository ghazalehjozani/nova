package ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.RespiteSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;

@Mapper(config = BaseMapperConfig.class)
public interface OriginateLoanFacilityApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "installmentScheduleId", ignore = true)
    @Mapping(target = "requestedAmount", ignore = true)
    @Mapping(target = "applicationNumber", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "guarantors", ignore = true)
    TradeLoanApplication.Builder map(OriginateLoanFacilityCommand.LoanApplicationDto loanApplication);

    Certificate map(OriginateLoanFacilityCommand.CertificateDto dto);

    CredibilityRank map(OriginateLoanFacilityCommand.CredibilityRankDto dto);

    Description map(OriginateLoanFacilityCommand.DescriptionDto dto);

    DisburseDestination map(OriginateLoanFacilityCommand.DisburseDestinationDto dto);

    RequestReason map(OriginateLoanFacilityCommand.RequestReasonDto dto);

    SubSource map(OriginateLoanFacilityCommand.SubSourceDto dto);

    EconomicSector map(OriginateLoanFacilityCommand.EconomicSectorDto dto);

    LoanDuration map(OriginateLoanFacilityCommand.LoanDurationDto dto);

    GracePeriod map(OriginateLoanFacilityCommand.GracePeriodDto dto);

    InstallmentCount map(OriginateLoanFacilityCommand.InstallmentCountDto dto);

    default Optional<DepositNumber> mapDepositNumber(@Nullable String depositNumber) {
        return depositNumber != null ? Optional.of(new DepositNumber(depositNumber)) : Optional.empty();
    }

    default Optional<RespiteSerial> mapRespiteSerial(@Nullable String respiteSerial) {
        return respiteSerial != null
                ? Optional.of(RespiteSerial.of(respiteSerial).getValue())
                : Optional.empty();
    }

    @AfterMapping
    default void fillCurrency(
            @MappingTarget TradeLoanApplication.Builder builder,
            OriginateLoanFacilityCommand.LoanApplicationDto loanApplication) {
        CurrencyType currency = map(loanApplication.currency());
        builder.requestedAmount(new Money(loanApplication.requestedAmount().value(), currency));
    }

    CurrencyType map(OriginateLoanFacilityCommand.CurrencyTypeDto dto);
}
