package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.mapper;

import java.time.LocalDate;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.ValueObjectMapper;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {
            ValueObjectMapper.class,
            TradeSanctionedLoanPersistenceMapper.class,
            TradeLoanApplicationPersistenceMapper.class
        })
public interface TradeLoanFacilityPersistenceMapper {

    @Mappings({
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "modifiedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "modifiedBy", ignore = true),
        @Mapping(target = "facilityType", constant = "TRADE"),
        @Mapping(
                target = "issueContractTransactionNumbers",
                source = "issueContractTransactionNumbers",
                qualifiedByName = "toTransactionNumberEmbList"),
        @Mapping(
                target = "disbursementTransactionNumbers",
                source = "disbursementTransactionNumbers",
                qualifiedByName = "toTransactionNumberEmbList"),
        @Mapping(target = "accountInfoMap", source = "accountInfoMap", qualifiedByName = "toAccountInfoMapEmb"),
        @Mapping(target = "disbursementDate", source = "disbursementDate"),
        @Mapping(target = "collaterals", source = "collaterals")
    })
    TradeLoanFacilityEntity map(TradeLoanFacility domain);

    @Mapping(target = "accountInfoMap", source = "accountInfoMap", qualifiedByName = "fromAccountInfoMapEmb")
    @Mapping(target = "collaterals", source = "collaterals")
    TradeLoanFacility map(TradeLoanFacilityEntity entity);

    default LocalDate map(Optional<LocalDate> optional) {
        return optional.orElse(null);
    }

    default Optional<LocalDate> map(LocalDate localDate) {
        return Optional.ofNullable(localDate);
    }
}
