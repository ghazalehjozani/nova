package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.vo.LoanTypeGroupCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity.LoanTypeGroupEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.mapper.LoanTypeGroupPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.repository.LoanTypeGroupJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanTypeGroupRepositoryAdapter implements LoanTypeGroupRepository {

    private final LoanTypeGroupJpaRepository jpaRepository;
    private final LoanTypeGroupPersistenceMapper mapper;

    @Override
    @Transactional
    public LoanTypeGroup save(LoanTypeGroup group) {
        LoanTypeGroupEntity saved = jpaRepository.save(requireNonNull(mapper.map(group)));
        return mapper.map(saved);
    }

    @Override
    @Transactional
    public LoanTypeGroup save(LoanTypeGroup group, long expectedVersion) {
        LoanTypeGroupEntity entity = requireNonNull(mapper.map(group));
        LoanTypeGroupEntity saved = jpaRepository.saveWithOptimisticLock(entity, expectedVersion);
        return mapper.map(saved);
    }

    @Override
    public Optional<LoanTypeGroup> findById(LoanTypeGroupId id) {
        return jpaRepository.findById(requireNonNull(id.value())).map(mapper::map);
    }

    @Override
    public Boolean existsById(LoanTypeGroupId id) {
        return jpaRepository.existsById(requireNonNull(id.value()));
    }

    @Override
    public Boolean existsByCode(LoanTypeGroupCode code) {
        return jpaRepository.existsByCode(code.value());
    }

    @Override
    public List<LoanTypeGroupId> findAncestorChain(LoanTypeGroupId id) {
        List<LoanTypeGroupId> chain = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        visited.add(requireNonNull(id.value()));
        Optional<LoanTypeGroupEntity> current = jpaRepository.findById(requireNonNull(id.value()));
        while (current.isPresent() && current.get().getParentGroupId() != null) {
            UUID parentValue = current.get().getParentGroupId();
            if (!visited.add(parentValue)) {
                break;
            }
            chain.add(new LoanTypeGroupId(parentValue));
            current = jpaRepository.findById(parentValue);
        }
        return List.copyOf(chain);
    }
}
