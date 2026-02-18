package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentPaymentRecord;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentPaymentEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.ValueObjectMapper;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {ValueObjectMapper.class})
public abstract class InstallmentPersistenceMapper {

    @Mapping(target = "payments", source = "paymentRecords")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "installmentSchedule", ignore = true)
    public abstract InstallmentEntity map(Installment domain);

    @Mapping(target = "paymentRecords", source = "payments")
    @Mapping(target = "version", source = "version")
    public abstract Installment map(InstallmentEntity entity);

    public abstract InstallmentPaymentEmb map(InstallmentPaymentRecord record);

    public abstract InstallmentPaymentRecord map(InstallmentPaymentEmb emb);
}
