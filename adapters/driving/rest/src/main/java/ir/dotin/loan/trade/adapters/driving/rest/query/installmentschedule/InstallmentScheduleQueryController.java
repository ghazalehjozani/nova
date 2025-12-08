package ir.dotin.loan.trade.adapters.driving.rest.query.installmentschedule;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.controller.BaseController;
import ir.dotin.platform.adapter.rest.response.DataResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.installmentschedule.dto.TradeInstallmentScheduleQueryDto;
import ir.dotin.loan.trade.core.application.query.installmentschedule.request.GetInstallmentScheduleByIdQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/installment-schedules")
@RequiredArgsConstructor
@Tag(name = SwaggerConfig.TAG_INSTALLMENT_SCHEDULE_QUERIES, description = "استعلام اقساط")
public class InstallmentScheduleQueryController extends BaseController {

    private final QueryDispatcher dispatcher;

    @GetMapping(value = "/{installmentScheduleId}", version = "1")
    @Operation(summary = "دریافت برنامه اقساط بر اساس شناسه")
    public DataResponse<TradeInstallmentScheduleQueryDto> getById(@PathVariable UUID installmentScheduleId) {
        GetInstallmentScheduleByIdQuery query = GetInstallmentScheduleByIdQuery.builder()
                .uid(getXRequestId())
                .installmentScheduleId(installmentScheduleId)
                .build();
        return DataResponse.of(dispatcher.dispatch(query));
    }
}
