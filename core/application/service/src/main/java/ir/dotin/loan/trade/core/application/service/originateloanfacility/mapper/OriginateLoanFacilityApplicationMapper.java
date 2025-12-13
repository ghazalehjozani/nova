package ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CredibilityRank;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Description;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DisburseDestination;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.GracePeriod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.RequestReason;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.RespiteSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.CurrencyTypeDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.EconomicSectorDto;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;

import static java.util.Objects.requireNonNull;

@Mapper(config = BaseMapperConfig.class)
public interface OriginateLoanFacilityApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestedAmount", ignore = true)
    @Mapping(target = "applicationNumber", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "parties", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "installmentCount", ignore = true)
    TradeLoanApplication.Builder map(OriginateLoanFacilityCommand.LoanApplicationDto loanApplication);

    CredibilityRank map(OriginateLoanFacilityCommand.CredibilityRankDto dto);

    Description map(OriginateLoanFacilityCommand.DescriptionDto dto);

    DisburseDestination map(OriginateLoanFacilityCommand.DisburseDestinationDto dto);

    RequestReason map(OriginateLoanFacilityCommand.RequestReasonDto dto);

    SubSource map(OriginateLoanFacilityCommand.SubSourceDto dto);

    EconomicSector map(EconomicSectorDto dto);

    LoanDuration map(OriginateLoanFacilityCommand.LoanDurationDto dto);

    GracePeriod map(OriginateLoanFacilityCommand.GracePeriodDto dto);

    @AfterMapping
    default void fillInstallmentCount(
            @MappingTarget TradeLoanApplication.Builder builder, OriginateLoanFacilityCommand command) {
        if (command.installmentSchedulePlan() != null) {
            builder.installmentCount(InstallmentCount.of(
                            command.installmentSchedulePlan().installments().size())
                    .orElseThrow());
        } else {
            Integer value = null;
            if (command.loanApplication().installmentCount() != null) {
                value = command.loanApplication().installmentCount().value();
            }
            builder.installmentCount(new InstallmentCount(requireNonNull(value)));
        }
    }

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

    CurrencyType map(CurrencyTypeDto dto);
}
