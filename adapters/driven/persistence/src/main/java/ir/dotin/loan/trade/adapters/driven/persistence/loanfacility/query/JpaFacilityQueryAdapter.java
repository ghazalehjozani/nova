package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query;

import java.util.List;
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

import ir.dotin.platform.pangaea.persistence.jpa.query.QueryCriteria;
import ir.dotin.platform.pangaea.persistence.jpa.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query.mapper.TradeLoanFacilityQueryMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.shared.query.AbstractCursorPagingAdapter;
import ir.dotin.loan.trade.adapters.driven.persistence.shared.query.SortBuilder;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.query.loanfacility.repository.TradeLoanFacilityQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.LoanFacilityFilterQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorEncoder;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPosition;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPage;

@Repository
public class JpaFacilityQueryAdapter extends AbstractCursorPagingAdapter<TradeLoanFacilityEntity, TradeFacilityQueryDto>
        implements TradeLoanFacilityQueryRepository {

    private final TradeLoanFacilityJpaRepository repository;
    private final TradeLoanFacilityQueryMapper mapper;

    public JpaFacilityQueryAdapter(
            TradeLoanFacilityJpaRepository repository,
            TradeLoanFacilityQueryMapper mapper,
            CursorEncoder cursorEncoder) {
        super(cursorEncoder);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CursorPage<TradeFacilityQueryDto> findAll(CursorPageRequest pageRequest) {
        ScrollPosition scrollPosition = buildScrollPosition(pageRequest);
        Sort sort = SortBuilder.buildCursorSort();
        Window<TradeLoanFacilityEntity> window =
                repository.findAllBy(scrollPosition, Limit.of(pageRequest.pageSize()), sort);
        return buildCursorPageFromWindow(window, pageRequest, mapper::toQueryModel);
    }

    @Override
    protected Window<TradeLoanFacilityEntity> findAllWithCursor(
            CursorPageRequest pageRequest, PersistentRepository<TradeLoanFacilityEntity> repository) {
        ScrollPosition scrollPosition = buildScrollPosition(pageRequest);
        Sort sort = SortBuilder.buildCursorSort();
        return ((TradeLoanFacilityJpaRepository) repository)
                .findAllBy(scrollPosition, Limit.of(pageRequest.pageSize()), sort);
    }

    @Override
    protected CursorPosition createCursorPosition(TradeFacilityQueryDto queryDto) {
        return CursorPosition.of(queryDto.id());
    }

    @Override
    public Optional<TradeFacilityQueryDto> findById(UUID facilityId) {
        return findById(facilityId, repository, mapper::toQueryModel);
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

    private Sort buildSort(ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPageRequest pageRequest) {
        return SortBuilder.buildOffsetSort(pageRequest);
    }
}
