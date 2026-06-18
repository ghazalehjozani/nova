package ir.dotin.loan.trade.adapters.driving.rest.query.formula;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.formula.dto.DiscoveryStatsView;
import ir.dotin.loan.trade.core.application.query.formula.dto.ProviderListView;
import ir.dotin.loan.trade.core.application.query.formula.dto.ProviderView;
import ir.dotin.loan.trade.core.application.query.formula.request.GetDiscoveryBindingsQuery;
import ir.dotin.loan.trade.core.application.query.formula.request.GetDiscoveryStatsQuery;
import ir.dotin.loan.trade.core.application.query.formula.request.GetProviderQuery;
import ir.dotin.loan.trade.core.application.query.formula.request.GetProvidersQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/formulas/discovery")
@Tag(name = SwaggerConfig.TAG_FORMULAS_QUERIES, description = "کشف ارائه‌دهندگان و اتصالات فرمول")
@RequiredArgsConstructor
class FormulaDiscoveryController extends BaseController {

    private final QueryDispatcher dispatcher;

    @GetMapping(value = "/providers", version = "1")
    @Operation(summary = "فهرست همه ارائه‌دهندگان")
    public ResponseEntity<BaseResponse<ProviderListView>> getAllProviders() {
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(new GetProvidersQuery())));
    }

    @GetMapping(value = "/providers/{code}", version = "1")
    @Operation(summary = "دریافت ارائه‌دهنده بر اساس کد")
    public ResponseEntity<BaseResponse<ProviderView>> getProvider(@PathVariable String code) {
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(new GetProviderQuery(code))));
    }

    @GetMapping(value = "/bindings", version = "1")
    @Operation(summary = "همه نام‌های اتصال")
    public ResponseEntity<BaseResponse<Set<String>>> getAllBindings() {
        return ResponseEntity.ok(BaseResponse.success(
                dispatcher.dispatch(new GetDiscoveryBindingsQuery()).bindingNames()));
    }

    @GetMapping(value = "/stats", version = "1")
    @Operation(summary = "آمار کشف اتصالات")
    public ResponseEntity<BaseResponse<DiscoveryStatsView>> getStats() {
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(new GetDiscoveryStatsQuery())));
    }
}
