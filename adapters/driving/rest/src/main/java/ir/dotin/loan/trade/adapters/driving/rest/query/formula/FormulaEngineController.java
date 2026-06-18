package ir.dotin.loan.trade.adapters.driving.rest.query.formula;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.formula.service.dto.EngineCapabilitiesDto;
import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.formula.dto.EngineCapabilitiesView;
import ir.dotin.loan.trade.core.application.query.formula.request.GetEngineCapabilitiesQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/engine")
@Tag(name = SwaggerConfig.TAG_FORMULAS_QUERIES, description = "قابلیت‌های موتور فرمول")
@RequiredArgsConstructor
class FormulaEngineController extends BaseController {

    private final QueryDispatcher dispatcher;

    @GetMapping(value = "/capabilities", version = "1")
    @Operation(summary = "قابلیت‌های موتور ارزیابی")
    public ResponseEntity<BaseResponse<EngineCapabilitiesDto>> getCapabilities() {
        EngineCapabilitiesView view = dispatcher.dispatch(new GetEngineCapabilitiesQuery());
        return ResponseEntity.ok(BaseResponse.success(view.capabilities()));
    }
}
