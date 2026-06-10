package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.query;

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
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.query.mapper.LoanArrangementQueryModelMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.repository.TradeLoanArrangementJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.shared.query.AbstractCursorPagingAdapter;
import ir.dotin.loan.trade.adapters.driven.persistence.shared.query.SortBuilder;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.query.loanarrangement.repository.TradeLoanArrangementQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.LoanTypeArrangementFilterQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorEncoder;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPosition;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPage;

@Repository
@Transactional(readOnly = true)
public class JpaTradeLoanArrangementQueryAdapter
        extends AbstractCursorPagingAdapter<TradeLoanArrangementEntity, TradeLoanArrangementQueryDto>
        implements TradeLoanArrangementQueryRepository {

    private final TradeLoanArrangementJpaRepository repository;
    private final LoanArrangementQueryModelMapper mapper;

    public JpaTradeLoanArrangementQueryAdapter(
            TradeLoanArrangementJpaRepository repository,
            LoanArrangementQueryModelMapper mapper,
            CursorEncoder cursorEncoder) {
        super(cursorEncoder);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CursorPage<TradeLoanArrangementQueryDto> findAll(CursorPageRequest pageRequest) {
        ScrollPosition scrollPosition = buildScrollPosition(pageRequest);
        Sort sort = SortBuilder.buildCursorSort();
        Window<TradeLoanArrangementEntity> window =
                repository.findAllBy(scrollPosition, Limit.of(pageRequest.pageSize()), sort);
        return buildCursorPageFromWindow(window, pageRequest, mapper::toQueryModel);
    }

    @Override
    protected Window<TradeLoanArrangementEntity> findAllWithCursor(
            CursorPageRequest pageRequest, PersistentRepository<TradeLoanArrangementEntity> repository) {
        ScrollPosition scrollPosition = buildScrollPosition(pageRequest);
        Sort sort = SortBuilder.buildCursorSort();
        return ((TradeLoanArrangementJpaRepository) repository)
                .findAllBy(scrollPosition, Limit.of(pageRequest.pageSize()), sort);
    }

    @Override
    protected CursorPosition createCursorPosition(TradeLoanArrangementQueryDto queryDto) {
        return CursorPosition.of(queryDto.id());
    }

    @Override
    public Optional<TradeLoanArrangementQueryDto> findById(UUID loanArrangementId) {
        return findById(loanArrangementId, repository, mapper::toQueryModel);
    }

    @Override
    public Optional<TradeLoanArrangementQueryDto> findByCode(String code) {
        return repository.getByCode(code).map(mapper::toQueryModel);
    }

    @Override
    public OffsetPage<TradeLoanArrangementQueryDto> findByFilter(LoanTypeArrangementFilterQuery filter) {
        Sort sort = buildSort(filter.offsetPageRequest());
        Pageable pageable = PageRequest.of(
                filter.offsetPageRequest().page(), filter.offsetPageRequest().pageSize(), sort);

        QueryCriteria<TradeLoanArrangementEntity> criteria = new QueryCriteria<>();

        Specification<TradeLoanArrangementEntity> spec = criteria.where("code", filter.code())
                .whereNested("title.value", filter.title())
                .whereNested("currencyType.value", filter.currencyType())
                .whereNested("economicSector.code", filter.economicSector())
                .where("active", filter.active())
                .where("disable", filter.disable())
                .where("disbursementMethod", filter.disbursementMethod())
                .whereNestedGreaterThanOrEqual("amountRange.minAmount", filter.minAmount())
                .whereNestedLessThanOrEqual("amountRange.maxAmount", filter.maxAmount())
                .toSpecification();

        Page<TradeLoanArrangementEntity> page = repository.findAll(spec, pageable);

        List<TradeLoanArrangementQueryDto> loanArrangements =
                page.getContent().stream().map(mapper::toQueryModel).toList();

        return new OffsetPage<>(
                loanArrangements,
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
