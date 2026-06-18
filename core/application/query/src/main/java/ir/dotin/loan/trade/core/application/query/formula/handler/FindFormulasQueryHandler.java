package ir.dotin.loan.trade.core.application.query.formula.handler;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.dto.FormulaDto;
import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.dto.FormulaPageView;
import ir.dotin.loan.trade.core.application.query.formula.request.FindFormulasQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFormulasQueryHandler implements QueryHandler<FindFormulasQuery, FormulaPageView> {

    private final FormulaQueryService formulaQueryService;

    @Override
    public FormulaPageView handle(FindFormulasQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.pageSize());
        Page<FormulaDto> page =
                switch (query.mode()) {
                    case ALL -> formulaQueryService.findAll(pageable);
                    case SEARCH -> formulaQueryService.searchByExpression(orEmpty(query.argument()), pageable);
                    case BY_PROVIDER -> formulaQueryService.findByProviderCode(orEmpty(query.argument()), pageable);
                    case DEPENDENTS -> formulaQueryService.findDependents(orEmpty(query.argument()), pageable);
                    case DEPENDENCIES -> formulaQueryService.findDependencies(orEmpty(query.argument()), pageable);
                };

        return FormulaPageView.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private static String orEmpty(@Nullable String value) {
        return value != null ? value : "";
    }
}
