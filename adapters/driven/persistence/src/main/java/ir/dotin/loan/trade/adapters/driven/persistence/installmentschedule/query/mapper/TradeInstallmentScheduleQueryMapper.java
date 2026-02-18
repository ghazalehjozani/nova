package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentPaymentEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RestructuringRecordEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ScheduleHistoryEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentScheduleEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.query.installmentschedule.dto.TradeInstallmentScheduleQueryDto;

@Mapper(config = BaseMapperConfig.class)
public interface TradeInstallmentScheduleQueryMapper {

    @Mapping(target = "gracePeriodDays", source = "gracePeriod.days")
    @Mapping(target = "currency", source = "currency.value")
    @Mapping(target = "totalLoanAmount", source = "totalLoanAmount.amount")
    @Mapping(target = "scheduleHistory", source = "scheduleHistory", qualifiedByName = "toScheduleHistoryDto")
    @Mapping(target = "restructuringRecord", source = "restructuringRecord")
    TradeInstallmentScheduleQueryDto toDto(InstallmentScheduleEntity entity);

    TradeInstallmentScheduleQueryDto.RestructuringRecordDto toRestructuringRecordDto(RestructuringRecordEmb emb);

    @Mapping(target = "totalAmount", source = "scheduledAmount.totalAmount.amount")
    @Mapping(target = "currency", source = "scheduledAmount.totalAmount.currency")
    @Mapping(target = "principalAmount", source = "scheduledAmount.principalAmount.amount")
    @Mapping(target = "interestAmount", source = "scheduledAmount.interestAmount.amount")
    @Mapping(target = "paidAmount", source = "paidAmount.amount")
    @Mapping(target = "outstandingAmount", source = "outstandingAmount.amount")
    @Mapping(target = "payments", source = "payments")
    TradeInstallmentScheduleQueryDto.InstallmentEntityDto toInstallmentDto(InstallmentEntity entity);

    @Mapping(target = "principalAmount", source = "principalAmount.amount")
    @Mapping(target = "interestAmount", source = "interestAmount.amount")
    @Mapping(target = "totalPaidAmount", source = "totalPaidAmount.amount")
    @Mapping(target = "installmentSequenceNumber", source = "installmentSequenceNumber")
    TradeInstallmentScheduleQueryDto.InstallmentPaymentDto toPaymentDto(InstallmentPaymentEmb emb);

    @Named("toScheduleHistoryDto")
    default TradeInstallmentScheduleQueryDto.ScheduleHistoryEmbDto toScheduleHistoryDto(ScheduleHistoryEmb emb) {
        if (emb == null || emb.getPreviousScheduleIds() == null) {
            return new TradeInstallmentScheduleQueryDto.ScheduleHistoryEmbDto(List.of());
        }
        return new TradeInstallmentScheduleQueryDto.ScheduleHistoryEmbDto(
                new ArrayList<>(emb.getPreviousScheduleIds()));
    }
}
