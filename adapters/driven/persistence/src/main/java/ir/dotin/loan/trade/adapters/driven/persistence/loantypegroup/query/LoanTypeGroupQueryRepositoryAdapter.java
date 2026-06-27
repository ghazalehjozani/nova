package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity.LoanTypeGroupEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.repository.LoanTypeGroupJpaRepository;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.repository.LoanTypeGroupQueryRepository;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanTypeGroupQueryRepositoryAdapter implements LoanTypeGroupQueryRepository {

    private final LoanTypeGroupJpaRepository groupRepository;
    private final TradeLoanTypeJpaRepository loanTypeRepository;

    @Override
    public List<LoanTypeGroupNodeDto> findAllGroups() {
        return groupRepository.findAll().stream()
                .map(LoanTypeGroupQueryRepositoryAdapter::toNode)
                .toList();
    }

    @Override
    public List<LoanTypeRefDto> findAllMemberships() {
        return loanTypeRepository.findGroupMemberships();
    }

    @Override
    public Optional<LoanTypeGroupNodeDto> findById(UUID id) {
        return groupRepository.findById(id).map(LoanTypeGroupQueryRepositoryAdapter::toNode);
    }

    private static LoanTypeGroupNodeDto toNode(LoanTypeGroupEntity entity) {
        return new LoanTypeGroupNodeDto(
                requireNonNull(entity.getId(), "loanTypeGroup id"),
                requireNonNull(entity.getTitle(), "loanTypeGroup title"),
                entity.getParentGroupId());
    }
}
