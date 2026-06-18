package ir.dotin.loan.trade.core.application.query.formula.dto;

import java.util.List;

import ir.dotin.platform.formula.service.dto.FormulaDto;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

import lombok.Builder;

@Builder
public record FormulaPageView(
        List<FormulaDto> formulas, int currentPage, int pageSize, long totalElements, int totalPages)
        implements QueryResult {

    public static FormulaPageView of(
            List<FormulaDto> formulas, int currentPage, int pageSize, long totalElements, int totalPages) {
        return FormulaPageView.builder()
                .formulas(formulas)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
