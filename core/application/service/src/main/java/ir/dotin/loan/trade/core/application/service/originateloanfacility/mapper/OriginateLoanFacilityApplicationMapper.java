package ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.AccountDisburseDestination;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CredibilityRank;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DepositDisburseDestination;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Description;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DisburseDestination;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.GracePeriod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.RequestReason;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.RespiteSerial;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.CurrencyTypeDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.DisburseDestinationDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.EconomicSectorDto;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;

@Mapper(config = BaseMapperConfig.class)
public interface OriginateLoanFacilityApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestedAmount", ignore = true)
    @Mapping(target = "applicationNumber", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "parties", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "installmentCount", ignore = true)
    TradeLoanApplication.Builder map(OriginateFacilityCommand.LoanApplicationDto loanApplication);

    default DisburseDestination toDisburseDestination(DisburseDestinationDto dto) {
        return switch (dto) {
            case DisburseDestinationDto.DepositDestinationDto(var depositNumber) ->
                DepositDisburseDestination.of(
                                DepositNumber.valueOf(depositNumber).unwrap())
                        .unwrap();
            case DisburseDestinationDto.AccountDestinationDto(var accountNumber) ->
                AccountDisburseDestination.of(AccountNumber.of(accountNumber).unwrap())
                        .unwrap();
        };
    }

    CredibilityRank map(OriginateFacilityCommand.CredibilityRankDto dto);

    Description map(OriginateFacilityCommand.DescriptionDto dto);

    RequestReason map(OriginateFacilityCommand.RequestReasonDto dto);

    SubSource map(OriginateFacilityCommand.SubSourceDto dto);

    EconomicSector map(EconomicSectorDto dto);

    LoanDuration map(OriginateFacilityCommand.LoanDurationDto dto);

    GracePeriod map(OriginateFacilityCommand.GracePeriodDto dto);

    default Optional<DepositNumber> mapDepositNumber(@Nullable String depositNumber) {
        return depositNumber != null ? Optional.of(new DepositNumber(depositNumber)) : Optional.empty();
    }

    default Optional<RespiteSerial> mapRespiteSerial(@Nullable String respiteSerial) {
        return respiteSerial != null
                ? Optional.of(RespiteSerial.of(respiteSerial).unwrap())
                : Optional.empty();
    }

    @AfterMapping
    default void fillCurrency(
            @MappingTarget TradeLoanApplication.Builder builder,
            OriginateFacilityCommand.LoanApplicationDto loanApplication) {
        CurrencyType currency = map(loanApplication.currency());
        builder.requestedAmount(new Money(loanApplication.requestedAmount().value(), currency));
    }

    CurrencyType map(CurrencyTypeDto dto);
}
