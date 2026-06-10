package ir.dotin.loan.trade.adapters.driving.rest.query.installmentschedule;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.installmentschedule.dto.TradeInstallmentScheduleQueryDto;
import ir.dotin.loan.trade.core.application.query.installmentschedule.request.GetInstallmentScheduleByIdQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/installment-schedules")
@RequiredArgsConstructor
@Tag(name = SwaggerConfig.TAG_INSTALLMENT_SCHEDULE_QUERIES, description = "استعلام اقساط")
class InstallmentScheduleQueryController extends BaseController {

    private final QueryDispatcher dispatcher;

    @GetMapping(value = "/{installmentScheduleId}", version = "1")
    @Operation(summary = "دریافت برنامه اقساط بر اساس شناسه")
    public ResponseEntity<BaseResponse<TradeInstallmentScheduleQueryDto>> getById(
            @PathVariable UUID installmentScheduleId) {
        GetInstallmentScheduleByIdQuery query = GetInstallmentScheduleByIdQuery.builder()
                .installmentScheduleId(installmentScheduleId)
                .build();
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(query)));
    }
}
