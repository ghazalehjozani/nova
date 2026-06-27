package ir.dotin.loan.trade.adapters.driving.rest.query.loantypegroup;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.partial.PartialResponse;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeListResult;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.GetLoanTypeGroupByIdQuery;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.GetLoanTypeGroupTreeQuery;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.ListRootLoanTypeGroupsQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-type-groups")
@Tag(name = SwaggerConfig.TAG_LOAN_TYPE_GROUP_QUERIES, description = "استعلام گروه نوع تسهیلات")
@RequiredArgsConstructor
class LoanTypeGroupQueryController extends BaseController {

    private final QueryDispatcher dispatcher;

    @GetMapping(value = "/tree", version = "1")
    @Operation(summary = "دریافت درخت گروه‌های نوع تسهیلات")
    public ResponseEntity<BaseResponse<LoanTypeGroupTreeDto>> tree(PartialResponse partial) {
        return ResponseEntity.ok(BaseResponse.success(
                dispatcher.dispatch(GetLoanTypeGroupTreeQuery.builder().build())));
    }

    @GetMapping(value = "/{groupId}", version = "1")
    @Operation(summary = "دریافت زیردرخت گروه بر اساس شناسه")
    public ResponseEntity<BaseResponse<LoanTypeGroupTreeDto>> byId(
            @PathVariable UUID groupId, PartialResponse partial) {
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(
                GetLoanTypeGroupByIdQuery.builder().groupId(groupId).build())));
    }

    @GetMapping(version = "1")
    @Operation(summary = "دریافت گروه‌های ریشه نوع تسهیلات")
    public ResponseEntity<BaseResponse<List<LoanTypeGroupTreeDto>>> roots(PartialResponse partial) {
        LoanTypeGroupTreeListResult result =
                dispatcher.dispatch(ListRootLoanTypeGroupsQuery.builder().build());
        return ResponseEntity.ok(BaseResponse.success(result.items()));
    }
}
