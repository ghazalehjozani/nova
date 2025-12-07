package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.ValueObjectMapper;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

@Mapper(config = BaseMapperConfig.class, uses = ValueObjectMapper.class)
public interface TradeLoanArrangementPersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(source = "code", target = "code", qualifiedByName = "loanArrangementCodeToString")
    @Mapping(source = "amountRange", target = "amountRange", qualifiedByName = "mapAmountRangeToEmb")
    @Mapping(source = "durationRange", target = "durationRange", qualifiedByName = "loanDurationToPeriodRangeEmb")
    @Mapping(source = "interestPolicy", target = "interestPolicy", qualifiedByName = "mapInterestPolicyToEmb")
    @Mapping(source = "installmentPolicy", target = "installmentPolicy", qualifiedByName = "mapInstallmentPolicyToEmb")
    @Mapping(source = "gracePeriodPolicy", target = "gracePeriodPolicy", qualifiedByName = "mapGracePeriodPolicyToEmb")
    @Mapping(source = "disbursementType", target = "disbursementType")
    TradeLoanArrangementEntity map(TradeLoanArrangement domain);

    @Mapping(source = "code", target = "code", qualifiedByName = "stringToLoanArrangementCode")
    @Mapping(source = "amountRange", target = "amountRange", qualifiedByName = "mapAmountRangeEmbToRange")
    @Mapping(source = "durationRange", target = "durationRange", qualifiedByName = "periodRangeEmbToLoanDuration")
    @Mapping(source = "interestPolicy", target = "interestPolicy", qualifiedByName = "mapInterestPolicyEmbToPolicy")
    @Mapping(
            source = "installmentPolicy",
            target = "installmentPolicy",
            qualifiedByName = "mapInstallmentPolicyEmbToPolicy")
    @Mapping(
            source = "gracePeriodPolicy",
            target = "gracePeriodPolicy",
            qualifiedByName = "mapGracePeriodPolicyEmbToPolicy")
    @Mapping(source = "penaltyPolicy", target = "penaltyPolicy", qualifiedByName = "mapPenaltyPolicyEmbToPolicy")
    @Mapping(source = "disbursementType", target = "disbursementType")
    TradeLoanArrangement map(TradeLoanArrangementEntity entity);

    default Set<TradeLoanArrangementEntity> mapToEntities(Set<TradeLoanArrangement> domains) {
        return domains.stream().map(this::map).collect(Collectors.toSet());
    }

    default Set<TradeLoanArrangement> mapToDomains(Set<TradeLoanArrangementEntity> entities) {
        return entities.stream().map(this::map).collect(Collectors.toSet());
    }
}
