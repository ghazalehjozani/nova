package ir.dotin.loan.trade.adapters.driven.persistence.loantype.query;

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
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.platform.pangaea.persistence.jpa.query.QueryCriteria;
import ir.dotin.platform.pangaea.persistence.jpa.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.query.mapper.TradeLoanTypeQueryModelMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.shared.query.AbstractCursorPagingAdapter;
import ir.dotin.loan.trade.adapters.driven.persistence.shared.query.SortBuilder;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.repository.TradeLoanTypeQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantype.request.LoanTypeFilterQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorEncoder;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPosition;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPage;

@Repository
@Transactional(readOnly = true)
public class JpaTradeLoanTypeQueryAdapter
        extends AbstractCursorPagingAdapter<TradeLoanTypeEntity, TradeLoanTypeQueryDto>
        implements TradeLoanTypeQueryRepository {

    private final TradeLoanTypeJpaRepository repository;
    private final TradeLoanTypeQueryModelMapper mapper;

    public JpaTradeLoanTypeQueryAdapter(
            TradeLoanTypeJpaRepository repository, TradeLoanTypeQueryModelMapper mapper, CursorEncoder cursorEncoder) {
        super(cursorEncoder);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CursorPage<TradeLoanTypeQueryDto> findAll(CursorPageRequest pageRequest) {
        ScrollPosition scrollPosition = buildScrollPosition(pageRequest);
        Sort sort = SortBuilder.buildCursorSort();
        Window<TradeLoanTypeEntity> window =
                repository.findAllBy(scrollPosition, Limit.of(pageRequest.pageSize()), sort);
        return buildCursorPageFromWindow(window, pageRequest, mapper::toQueryModel);
    }

    @Override
    protected Window<TradeLoanTypeEntity> findAllWithCursor(
            CursorPageRequest pageRequest, PersistentRepository<TradeLoanTypeEntity> repository) {
        ScrollPosition scrollPosition = buildScrollPosition(pageRequest);
        Sort sort = SortBuilder.buildCursorSort();
        return ((TradeLoanTypeJpaRepository) repository)
                .findAllBy(scrollPosition, Limit.of(pageRequest.pageSize()), sort);
    }

    @Override
    protected CursorPosition createCursorPosition(TradeLoanTypeQueryDto queryDto) {
        return CursorPosition.of(queryDto.id());
    }

    @Override
    public Optional<TradeLoanTypeQueryDto> findById(UUID loanTypeId) {
        return findById(loanTypeId, repository, mapper::toQueryModel);
    }

    @Override
    public Optional<TradeLoanTypeQueryDto> findByCode(String code) {
        return repository.getByCode_Value(code).map(mapper::toQueryModel);
    }

    @Override
    public OffsetPage<TradeLoanTypeQueryDto> findByFilter(LoanTypeFilterQuery filter) {
        Sort sort = buildSort(filter.offsetPageRequest());
        Pageable pageable = PageRequest.of(
                filter.offsetPageRequest().page(), filter.offsetPageRequest().pageSize(), sort);

        QueryCriteria<TradeLoanTypeEntity> criteria = new QueryCriteria<>();

        Specification<TradeLoanTypeEntity> spec = criteria.whereNested("code.value", filter.code())
                .whereNested("title.value", filter.title())
                .toSpecification();

        Page<TradeLoanTypeEntity> page = repository.findAll(spec, pageable);

        List<TradeLoanTypeQueryDto> loanTypes =
                page.getContent().stream().map(mapper::toQueryModel).toList();

        return new OffsetPage<>(
                loanTypes,
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
