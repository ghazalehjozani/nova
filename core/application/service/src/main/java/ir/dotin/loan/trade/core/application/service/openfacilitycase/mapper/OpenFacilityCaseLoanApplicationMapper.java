package ir.dotin.loan.trade.core.application.service.openfacilitycase.mapper;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.commons.domain.vo.NationalCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Certificate;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CredibilityRank;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Description;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DisburseDestination;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.GracePeriod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.RequestReason;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.RespiteSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.PersonName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OpenFacilityCaseCommand;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;

@Mapper(config = BaseMapperConfig.class)
public interface OpenFacilityCaseLoanApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "installmentScheduleId", ignore = true)
    TradeLoanApplication map(OpenFacilityCaseCommand.LoanApplicationDto loanApplication);

    @Mapping(target = "nationalCode", ignore = true)
    Party map(OpenFacilityCaseCommand.PartyDto dto);

    PersonName map(OpenFacilityCaseCommand.PersonNameDto dto);

    Branch map(OpenFacilityCaseCommand.BranchDto dto);

    Certificate map(OpenFacilityCaseCommand.CertificateDto dto);

    CredibilityRank map(OpenFacilityCaseCommand.CredibilityRankDto dto);

    Description map(OpenFacilityCaseCommand.DescriptionDto dto);

    DisburseDestination map(OpenFacilityCaseCommand.DisburseDestinationDto dto);

    RequestReason map(OpenFacilityCaseCommand.RequestReasonDto dto);

    SubSource map(OpenFacilityCaseCommand.SubSourceDto dto);

    ApplicationNumber map(OpenFacilityCaseCommand.ApplicationNumberDto dto);

    LoanTypeCode map(OpenFacilityCaseCommand.LoanTypeCodeDto dto);

    EconomicSector map(OpenFacilityCaseCommand.EconomicSectorDto dto);

    LoanDuration map(OpenFacilityCaseCommand.LoanDurationDto dto);

    GracePeriod map(OpenFacilityCaseCommand.GracePeriodDto dto);

    InstallmentCount map(OpenFacilityCaseCommand.InstallmentCountDto dto);

    Money map(OpenFacilityCaseCommand.MoneyDto dto);

    NationalCode map(OpenFacilityCaseCommand.NationalCodeDto dto);

    default BranchCode mapBranchCode(String code) {
        return BranchCode.of(code).orElseThrow();
    }

    default Optional<DepositNumber> mapDepositNumber(@Nullable String depositNumber) {
        return depositNumber != null ? Optional.of(new DepositNumber(depositNumber)) : Optional.empty();
    }

    default Optional<RespiteSerial> mapRespiteSerial(@Nullable String respiteSerial) {
        return respiteSerial != null
                ? Optional.of(RespiteSerial.of(respiteSerial).getValue())
                : Optional.empty();
    }
}
