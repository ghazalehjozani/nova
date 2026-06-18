package ir.dotin.loan.trade.core.application.query.formula.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.core.binding.BindingDiscoveryService;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.dto.ProviderView;
import ir.dotin.loan.trade.core.application.query.formula.i18n.FormulaQueryErrorCodes;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.loan.trade.core.application.query.formula.request.GetProviderQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProviderQueryHandler implements QueryHandler<GetProviderQuery, ProviderView> {

    private final BindingDiscoveryService bindingDiscoveryService;

    @Override
    public ProviderView handle(GetProviderQuery query) {
        return bindingDiscoveryService
                .getProvider(query.code())
                .map(ProviderView::new)
                .orElseThrow(() -> new FailureCauseException(FailureCause.notFound(
                        Notification.ofError(FormulaQueryErrorCodes.PROVIDER_NOT_FOUND, query.code()))));
    }
}
