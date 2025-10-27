package ir.dotin.loan.trade.core.application.service.openfacilitycase.mapper;

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
import ir.dotin.loan.trade.core.application.ports.inbound.command.OpenFacilityCaseCommand;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;

@Mapper(config = BaseMapperConfig.class)
public interface OpenFacilityCaseLoanApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "installmentScheduleId", ignore = true)
    @Mapping(target = "requestedAmount", ignore = true)
    @Mapping(target = "applicationNumber", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "guarantors", ignore = true)
    TradeLoanApplication.Builder map(OpenFacilityCaseCommand.LoanApplicationDto loanApplication);

    Certificate map(OpenFacilityCaseCommand.CertificateDto dto);

    CredibilityRank map(OpenFacilityCaseCommand.CredibilityRankDto dto);

    Description map(OpenFacilityCaseCommand.DescriptionDto dto);

    DisburseDestination map(OpenFacilityCaseCommand.DisburseDestinationDto dto);

    RequestReason map(OpenFacilityCaseCommand.RequestReasonDto dto);

    SubSource map(OpenFacilityCaseCommand.SubSourceDto dto);

    EconomicSector map(OpenFacilityCaseCommand.EconomicSectorDto dto);

    LoanDuration map(OpenFacilityCaseCommand.LoanDurationDto dto);

    GracePeriod map(OpenFacilityCaseCommand.GracePeriodDto dto);

    InstallmentCount map(OpenFacilityCaseCommand.InstallmentCountDto dto);

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
            OpenFacilityCaseCommand.LoanApplicationDto loanApplication) {
        CurrencyType currency = map(loanApplication.currency());
        builder.requestedAmount(new Money(loanApplication.requestedAmount().value(), currency));
    }

    CurrencyType map(OpenFacilityCaseCommand.CurrencyTypeDto dto);
}
