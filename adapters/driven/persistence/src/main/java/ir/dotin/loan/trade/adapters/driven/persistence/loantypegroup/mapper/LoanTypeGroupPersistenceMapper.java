package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.mapper;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity.LoanTypeGroupEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;

import static java.util.Objects.requireNonNull;

@Mapper(config = BaseMapperConfig.class)
public interface LoanTypeGroupPersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    LoanTypeGroupEntity map(LoanTypeGroup domain);

    default LoanTypeGroup map(LoanTypeGroupEntity entity) {
        return LoanTypeGroup.reconstitute(
                new LoanTypeGroupId(requireNonNull(entity.getId(), "loanTypeGroup id")),
                new Title(requireNonNull(entity.getTitle(), "loanTypeGroup title")),
                toGroupId(entity.getParentGroupId()),
                toVersion(entity.getVersion()));
    }

    default @Nullable UUID map(@Nullable LoanTypeGroupId id) {
        return id == null ? null : id.value();
    }

    default String map(Title title) {
        return title.value();
    }

    private @Nullable LoanTypeGroupId toGroupId(@Nullable UUID value) {
        return value == null ? null : new LoanTypeGroupId(value);
    }

    private @Nullable Long toVersion(@Nullable Integer version) {
        return version == null ? null : version.longValue();
    }
}
