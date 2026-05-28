package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.time.LocalDate;
import java.util.Map;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LumpSumDisbursementRequest", description = "درخواست پرداخت یکجای تسهیلات")
public record LumpSumDisbursementRequest(
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Long version,

        @Schema(description = "تاریخ پرداخت") LocalDate disbursementDate,

        @Schema(
                description = "متادیتای سند تراکنش (پارامترهای پایانه، کانال، محصول و شبکه)",
                example = "{\"terminalId\":\"1\",\"terminalType\":\"Branch\",\"channel\":\"Branch\","
                        + "\"toolSource\":\"BANK\",\"productCode\":\"LOAN\",\"networkType\":\"BankBook\"}")
        Map<String, String> metadata)
        implements BaseRequest {}
