package ir.dotin.loan.trade.core.application.query.formula.request;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.formula.dto.FormulaPageView;

import lombok.Builder;

@Builder
public record FindFormulasQuery(Mode mode, @Nullable String argument, int page, int pageSize)
        implements Query<FormulaPageView> {

    public FindFormulasQuery {
        if (mode == null) {
            mode = Mode.ALL;
        }
        if (page < 0) {
            page = 0;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }
    }

    @Override
    public Class<FormulaPageView> getResultType() {
        return FormulaPageView.class;
    }

    public static FindFormulasQuery all(int page, int pageSize) {
        return new FindFormulasQuery(Mode.ALL, null, page, pageSize);
    }

    public static FindFormulasQuery search(@Nullable String expression, int page, int pageSize) {
        return new FindFormulasQuery(Mode.SEARCH, expression, page, pageSize);
    }

    public static FindFormulasQuery byProvider(@Nullable String providerCode, int page, int pageSize) {
        return new FindFormulasQuery(Mode.BY_PROVIDER, providerCode, page, pageSize);
    }

    public static FindFormulasQuery dependents(@Nullable String code, int page, int pageSize) {
        return new FindFormulasQuery(Mode.DEPENDENTS, code, page, pageSize);
    }

    public static FindFormulasQuery dependencies(@Nullable String code, int page, int pageSize) {
        return new FindFormulasQuery(Mode.DEPENDENCIES, code, page, pageSize);
    }

    public enum Mode {
        ALL,
        SEARCH,
        BY_PROVIDER,
        DEPENDENTS,
        DEPENDENCIES
    }
}
