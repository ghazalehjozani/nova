package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.platform.adapter.persistence.query.QueryCriteria;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query.mapper.FacilityQueryModelMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.query.loanfacility.repository.TradeLoanFacilityQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.LoanFacilityFilterQuery;
import ir.dotin.loan.trade.core.application.query.shared.exception.InvalidCursorException;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorEncoder;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPosition;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPage;

import lombok.RequiredArgsConstructor;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JpaFacilityQueryAdapter implements TradeLoanFacilityQueryRepository {

    private final TradeLoanFacilityJpaRepository repository;
    private final FacilityQueryModelMapper mapper;
    private final CursorEncoder cursorEncoder;

    @Override
    public Optional<TradeFacilityQueryDto> findById(UUID facilityId) {
        return repository.findById(facilityId).map(mapper::toQueryModel);
    }

    @Override
    public CursorPage<TradeFacilityQueryDto> findAll(CursorPageRequest pageRequest) {
        ScrollPosition scrollPosition = buildScrollPosition(pageRequest);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));

        Window<TradeLoanFacilityEntity> window =
                repository.findAllBy(scrollPosition, Limit.of(pageRequest.pageSize()), sort);

        return buildCursorPage(window, pageRequest);
    }

    @Override
    public OffsetPage<TradeFacilityQueryDto> findByFilter(LoanFacilityFilterQuery filter) {
        Sort sort = buildSort(filter.offsetPageRequest());
        Pageable pageable = PageRequest.of(
                filter.offsetPageRequest().page(), filter.offsetPageRequest().pageSize(), sort);

        QueryCriteria<TradeLoanFacilityEntity> criteria = new QueryCriteria<>();

        Specification<TradeLoanFacilityEntity> spec = criteria.where("loanTypeId", filter.loanTypeId())
                .whereNested("loanApplication.customer.customerNumber", filter.customerNumber())
                .whereGreaterThanOrEqual("createdAt", filter.createDateFrom())
                .whereLessThanOrEqual("createdAt", filter.createDateTo())
                .whereNestedGreaterThanOrEqual("loanApplication.requestedAmount.amount", filter.requestAmountMin())
                .whereNestedLessThanOrEqual("loanApplication.requestedAmount.amount", filter.requestAmountMax())
                .where("currentState", filter.status())
                .toSpecification();

        Page<TradeLoanFacilityEntity> page = repository.findAll(spec, pageable);

        List<TradeFacilityQueryDto> facilities =
                page.getContent().stream().map(mapper::toQueryModel).toList();

        return new OffsetPage<>(
                facilities,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious());
    }

    private ScrollPosition buildScrollPosition(CursorPageRequest pageRequest) {
        if (pageRequest.cursor() == null || pageRequest.cursor().isBlank()) {
            return ScrollPosition.keyset();
        }

        try {
            cursorEncoder.validate(pageRequest.cursor());
            CursorPosition position = cursorEncoder.decode(pageRequest.cursor());

            return ScrollPosition.forward(Map.of(
                    "createdAt", position.timestamp(),
                    "id", position.id()));
        } catch (Exception e) {
            throw new InvalidCursorException("Failed to decode cursor position", e);
        }
    }

    private CursorPage<TradeFacilityQueryDto> buildCursorPage(
            Window<TradeLoanFacilityEntity> window, CursorPageRequest pageRequest) {
        List<TradeFacilityQueryDto> facilities =
                window.getContent().stream().map(mapper::toQueryModel).toList();

        String nextCursor = null;
        String previousCursor = pageRequest.cursor();

        if (window.hasNext() && !facilities.isEmpty()) {
            TradeFacilityQueryDto lastFacility = facilities.getLast();
            CursorPosition position = CursorPosition.of(lastFacility.createdAt(), lastFacility.id());
            nextCursor = cursorEncoder.encode(position);
        }

        return new CursorPage<>(facilities, nextCursor, previousCursor, window.hasNext(), !pageRequest.isFirstPage());
    }

    private Sort buildSort(ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPageRequest pageRequest) {
        Sort.Direction direction = pageRequest.direction()
                        == ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPageRequest.SortDirection
                                .ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, pageRequest.sortBy()).and(Sort.by(Sort.Direction.DESC, "id"));
    }
}
