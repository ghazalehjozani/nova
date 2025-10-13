package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.mapper;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.DisbursementSchedule;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.DisbursementScheduleEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.ValueObjectMapper;

@Mapper(config = BaseMapperConfig.class, uses = ValueObjectMapper.class)
public interface tradeDisbursementSchedulePersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    DisbursementScheduleEntity map(DisbursementSchedule domain);

    default DisbursementScheduleEntity map(Optional<DisbursementSchedule> domain) {
        return domain.map(this::map).orElse(null);
    }

    DisbursementSchedule map(DisbursementScheduleEntity entity);
}
