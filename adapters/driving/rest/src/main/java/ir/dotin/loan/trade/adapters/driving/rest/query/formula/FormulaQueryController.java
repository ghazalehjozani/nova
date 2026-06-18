package ir.dotin.loan.trade.adapters.driving.rest.query.formula;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.formula.service.cqrs.query.EvaluateFormulaQuery;
import ir.dotin.platform.formula.service.cqrs.query.EvaluateFormulaResult;
import ir.dotin.platform.formula.service.cqrs.query.FormulaView;
import ir.dotin.platform.formula.service.cqrs.query.GetFormulaQuery;
import ir.dotin.platform.formula.service.dto.FormulaDto;
import ir.dotin.platform.formula.service.dto.RegisteredBindingDto;
import ir.dotin.platform.formula.service.dto.ValidationResultDto;
import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.core.util.PagedResponseUtils;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.contract.dto.EvaluateFormulaRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.ValidateExpressionRequest;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.EvaluateFormulaRequestToQueryMapper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.formula.dto.FormulaExistsView;
import ir.dotin.loan.trade.core.application.query.formula.dto.FormulaPageView;
import ir.dotin.loan.trade.core.application.query.formula.dto.RegisteredBindingsView;
import ir.dotin.loan.trade.core.application.query.formula.dto.ProviderTypesView;
import ir.dotin.loan.trade.core.application.query.formula.dto.ValidationView;
import ir.dotin.loan.trade.core.application.query.formula.request.FindFormulasQuery;
import ir.dotin.loan.trade.core.application.query.formula.request.FormulaExistsQuery;
import ir.dotin.loan.trade.core.application.query.formula.request.GetRegisteredBindingsQuery;
import ir.dotin.loan.trade.core.application.query.formula.request.GetRegisteredProviderTypesQuery;
import ir.dotin.loan.trade.core.application.query.formula.request.ValidateExpressionQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/formulas")
@Tag(name = SwaggerConfig.TAG_FORMULAS_QUERIES, description = "استعلام و ارزیابی فرمول‌ها")
@RequiredArgsConstructor
class FormulaQueryController extends BaseController {

    private final QueryDispatcher dispatcher;
    private final EvaluateFormulaRequestToQueryMapper evaluateMapper;

    @PostMapping(value = "/{code}/evaluate", version = "1")
    @Operation(summary = "ارزیابی فرمول")
    public ResponseEntity<BaseResponse<EvaluateFormulaResult>> evaluate(
            @PathVariable String code, @RequestBody @Valid EvaluateFormulaRequest request) {
        EvaluateFormulaQuery query = evaluateMapper.toQuery(code, request);
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(query)));
    }

    @GetMapping(version = "1")
    @Operation(summary = "فهرست فرمول‌ها")
    public ResponseEntity<BaseResponse<List<FormulaDto>>> findAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
        FormulaPageView view = dispatcher.dispatch(FindFormulasQuery.all(page, pageSize));
        return page(view);
    }

    @GetMapping(value = "/search", version = "1")
    @Operation(summary = "جستجوی فرمول‌ها بر اساس عبارت")
    public ResponseEntity<BaseResponse<List<FormulaDto>>> search(
            @RequestParam String expression,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        FormulaPageView view = dispatcher.dispatch(FindFormulasQuery.search(expression, page, pageSize));
        return page(view);
    }

    @GetMapping(value = "/by-provider", version = "1")
    @Operation(summary = "فرمول‌ها بر اساس کد ارائه‌دهنده")
    public ResponseEntity<BaseResponse<List<FormulaDto>>> findByProvider(
            @RequestParam String providerCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        FormulaPageView view = dispatcher.dispatch(FindFormulasQuery.byProvider(providerCode, page, pageSize));
        return page(view);
    }

    @GetMapping(value = "/{code}/dependents", version = "1")
    @Operation(summary = "فرمول‌های وابسته به این فرمول")
    public ResponseEntity<BaseResponse<List<FormulaDto>>> dependents(
            @PathVariable String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        FormulaPageView view = dispatcher.dispatch(FindFormulasQuery.dependents(code, page, pageSize));
        return page(view);
    }

    @GetMapping(value = "/{code}/dependencies", version = "1")
    @Operation(summary = "وابستگی‌های این فرمول")
    public ResponseEntity<BaseResponse<List<FormulaDto>>> dependencies(
            @PathVariable String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        FormulaPageView view = dispatcher.dispatch(FindFormulasQuery.dependencies(code, page, pageSize));
        return page(view);
    }

    @GetMapping(value = "/{code}/exists", version = "1")
    @Operation(summary = "بررسی وجود فرمول")
    public ResponseEntity<BaseResponse<Boolean>> exists(@PathVariable String code) {
        FormulaExistsView view = dispatcher.dispatch(new FormulaExistsQuery(code));
        return ResponseEntity.ok(BaseResponse.success(view.exists()));
    }

    @PostMapping(value = "/validate", version = "1")
    @Operation(summary = "اعتبارسنجی نحوی عبارت فرمول")
    public ResponseEntity<BaseResponse<ValidationResultDto>> validate(
            @RequestBody @Valid ValidateExpressionRequest request) {
        ValidationView view = dispatcher.dispatch(new ValidateExpressionQuery(request.expression()));
        return ResponseEntity.ok(BaseResponse.success(view.result()));
    }

    @GetMapping(value = "/bindings", version = "1")
    @Operation(summary = "همه اتصالات ثبت‌شده")
    public ResponseEntity<BaseResponse<Set<RegisteredBindingDto>>> getAllBindings() {
        RegisteredBindingsView view = dispatcher.dispatch(new GetRegisteredBindingsQuery(null));
        return ResponseEntity.ok(BaseResponse.success(view.bindings()));
    }

    @GetMapping(value = "/bindings/by-provider", version = "1")
    @Operation(summary = "اتصالات ثبت‌شده یک ارائه‌دهنده")
    public ResponseEntity<BaseResponse<Set<RegisteredBindingDto>>> getBindingsByProvider(
            @RequestParam String providerCode) {
        RegisteredBindingsView view = dispatcher.dispatch(new GetRegisteredBindingsQuery(providerCode));
        return ResponseEntity.ok(BaseResponse.success(view.bindings()));
    }

    @GetMapping(value = "/bindings/providers", version = "1")
    @Operation(summary = "کدهای ارائه‌دهنده دارای اتصال")
    public ResponseEntity<BaseResponse<Set<String>>> getProviderTypes() {
        ProviderTypesView view = dispatcher.dispatch(new GetRegisteredProviderTypesQuery());
        return ResponseEntity.ok(BaseResponse.success(view.providerTypes()));
    }

    @GetMapping(value = "/{code}", version = "1")
    @Operation(summary = "دریافت فرمول بر اساس کد")
    public ResponseEntity<BaseResponse<FormulaView>> getByCode(@PathVariable String code) {
        GetFormulaQuery query = new GetFormulaQuery(UUID.randomUUID(), code);
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(query)));
    }

    private static ResponseEntity<BaseResponse<List<FormulaDto>>> page(FormulaPageView view) {
        return ResponseEntity.ok(PagedResponseUtils.offset(
                view.formulas(), view.currentPage(), view.pageSize(), view.totalElements()));
    }
}
