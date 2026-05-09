package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query.mapper;

import java.text.MessageFormat;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ApplicationNumberEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CancellationDataEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CloseFacilityPaidOffInfoEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CollateralEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.DisbursementRecordEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ScheduledTrancheEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;

@Mapper(config = BaseMapperConfig.class)
public interface TradeLoanFacilityQueryMapper {

    @Mapping(target = "totalDisbursedAmountCurrency", source = "totalDisbursedAmount.currency")
    @Mapping(target = "totalDisbursedAmount", source = "totalDisbursedAmount.amount")
    @Mapping(target = "sanctionedLoan.revocationReason", source = "sanctionedLoan.revocationReason.text")
    @Mapping(
            target = "sanctionedLoan.disbursementHistoryRecords",
            source = "sanctionedLoan.disbursementHistory.records")
    @Mapping(
            target = "sanctionedLoan.disbursementScheduleTranches",
            source = "sanctionedLoan.disbursementSchedule.tranches")
    @Mapping(target = "sanctionedLoan.loanDurationMonths", source = "sanctionedLoan.loanDuration.months")
    @Mapping(target = "sanctionedLoan.installmentCount", source = "sanctionedLoan.installmentCount.value")
    @Mapping(target = "sanctionedLoan.gracePeriodDays", source = "sanctionedLoan.gracePeriod.days")
    @Mapping(target = "sanctionedLoan.currency", source = "sanctionedLoan.currency.value")
    @Mapping(target = "sanctionedLoan.approvedAmount", source = "sanctionedLoan.approvedAmount.amount")
    @Mapping(target = "sanctionedLoan.sanctionSerialType", source = "sanctionedLoan.sanctionSerial.type")
    @Mapping(target = "sanctionedLoan.sanctionSerial", source = "sanctionedLoan.sanctionSerial.value")
    @Mapping(target = "sanctionedLoan.confirmType", source = "sanctionedLoan.confirmType.personCode")
    @Mapping(target = "loanApplication.applicationNumber", qualifiedByName = "formatAppNum")
    @Mapping(target = "loanApplication.loanTypeCode", source = "loanApplication.applicationNumber.loanTypeCode.value")
    @Mapping(target = "loanApplication.credibilityRank", source = "loanApplication.credibilityRank.value")
    @Mapping(target = "loanApplication.description", source = "loanApplication.description.value")
    @Mapping(target = "loanApplication.subSourceCode", source = "loanApplication.subSource.code")
    @Mapping(target = "loanApplication.requestReasonCode", source = "loanApplication.requestReason.code")
    @Mapping(target = "loanApplication.branchCode", source = "loanApplication.branch.code")
    @Mapping(target = "loanApplication.economicSectorCode", source = "loanApplication.economicSector.code")
    @Mapping(target = "loanApplication.disburseDestinationType", source = "loanApplication.disburseDestination.type")
    @Mapping(
            target = "loanApplication.disburseDestinationDepositNumber",
            source = "loanApplication.disburseDestination.depositNumber")
    @Mapping(
            target = "loanApplication.disburseDestinationAccountNumber",
            source = "loanApplication.disburseDestination.accountNumber")
    @Mapping(target = "loanApplication.installmentCount", source = "loanApplication.installmentCount.value")
    @Mapping(target = "loanApplication.gracePeriodDays", source = "loanApplication.gracePeriod.days")
    @Mapping(
            target = "loanApplication.requestedLoanDurationMonths",
            source = "loanApplication.requestedLoanDuration.months")
    @Mapping(target = "loanApplication.currency", source = "loanApplication.currency.value")
    @Mapping(target = "loanApplication.requestedAmountCurrency", source = "loanApplication.requestedAmount.currency")
    @Mapping(target = "loanApplication.requestedAmount", source = "loanApplication.requestedAmount.amount")
    @Mapping(target = "cancellationData.cancelReason", source = "cancellationDataEmb.cancelReason")
    @Mapping(target = "cancellationData.cancelDate", source = "cancellationDataEmb.cancelDate")
    @Mapping(target = "cancellationData.cancelDescription", source = "cancellationDataEmb.cancelDescription")
    @Mapping(target = "closePaidOff", source = "closeFacilityPaidOffInfo", qualifiedByName = "toClosePaidOffDto")
    TradeFacilityQueryDto toQueryModel(TradeLoanFacilityEntity tradeLoanFacilityEntity);

    @Named("formatAppNum")
    default String formattedApplicationNumber(ApplicationNumberEmb emb) {
        return MessageFormat.format(
                "{0}-{1}-{2}-{3}",
                emb.getBranch().getCode(),
                emb.getLoanTypeCode().getValue(),
                emb.getParty().getCustomerNumber(),
                emb.getDerivedValue());
    }

    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    TradeFacilityQueryDto.TradeSanctionedLoanEntityDto.ScheduledTrancheEmbDto toTrancheDto(ScheduledTrancheEmb emb);

    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    TradeFacilityQueryDto.TradeSanctionedLoanEntityDto.DisbursementRecordEmbDto toDisbursementDto(
            DisbursementRecordEmb emb);

    @Mapping(target = "collateralTypeCode", source = "collateralType")
    @Mapping(target = "collateralSerial", source = "collateralSerial.value")
    @Mapping(target = "usedAmount", source = "usedAmount.amount")
    @Mapping(target = "usedAmountCurrency", source = "usedAmount.currency")
    TradeFacilityQueryDto.CollateralEmbDto toCollateralDto(CollateralEmb emb);

    @Mapping(target = "cancelDescription", source = "cancelDescription")
    @Mapping(target = "cancelReason", source = "cancelReason")
    @Mapping(target = "cancelDate", source = "cancelDate")
    TradeFacilityQueryDto.CancellationData toCancellationDataEmbDto(CancellationDataEmb emb);

    @Named("toClosePaidOffDto")
    @Mapping(target = "closePaidOffDate", source = "closePaidOffDate")
    @Mapping(target = "closePaidOffTransactionReference", source = "closePaidOffTransactionReference")
    @Mapping(target = "totalClosePaidOffAmount", source = "totalClosePaidOffAmount.amount")
    @Mapping(target = "totalClosePaidOffAmountCurrency", source = "totalClosePaidOffAmount.currency")
    TradeFacilityQueryDto.ClosePaidOffDto toClosePaidOffDto(CloseFacilityPaidOffInfoEmb emb);
}
