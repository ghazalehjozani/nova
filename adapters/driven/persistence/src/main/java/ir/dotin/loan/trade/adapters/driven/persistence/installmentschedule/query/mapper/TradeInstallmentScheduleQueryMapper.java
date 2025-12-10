package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RestructuringRecordEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentScheduleEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.query.installmentschedule.dto.TradeInstallmentScheduleQueryDto;

@Mapper(config = BaseMapperConfig.class)
public interface TradeInstallmentScheduleQueryMapper {

    @Mapping(target = "gracePeriodDays", source = "gracePeriod.days")
    @Mapping(target = "currency", source = "currency.value")
    @Mapping(target = "totalLoanAmount", source = "totalLoanAmount.amount")
    TradeInstallmentScheduleQueryDto toDto(InstallmentScheduleEntity installmentScheduleEntity);

    TradeInstallmentScheduleQueryDto.RestructuringRecordDto toRestructuringRecordDto(RestructuringRecordEmb emb);

    @Mapping(target = "totalAmount", source = "scheduledAmount.totalAmount.amount")
    @Mapping(target = "currency", source = "scheduledAmount.totalAmount.currency")
    @Mapping(target = "principalAmount", source = "scheduledAmount.principalAmount.amount")
    @Mapping(target = "interestAmount", source = "scheduledAmount.interestAmount.amount")
    @Mapping(target = "paidAmount", source = "paidAmount.amount")
    @Mapping(target = "outstandingAmount", source = "outstandingAmount.amount")
    TradeInstallmentScheduleQueryDto.InstallmentEntityDto toInstallmentDto(InstallmentEntity entity);
}
