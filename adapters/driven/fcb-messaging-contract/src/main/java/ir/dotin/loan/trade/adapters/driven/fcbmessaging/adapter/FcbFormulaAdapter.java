package ir.dotin.loan.trade.adapters.driven.fcbmessaging.adapter;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.EvaluateFormulaViaFcbResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ValidateFormulaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.EvaluateFormulaViaFcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ValidateFormulaRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.FcbFormulaMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.formula.EvaluateFormulaViaFcbPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.formula.ValidateFormulaInFcbPort;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcbFormulaAdapter implements ValidateFormulaInFcbPort, EvaluateFormulaViaFcbPort {

    private final FcbRequestReplyClient kafkaClient;

    @Value("${nova.fcb.kafka.default-timeout:10s}")
    private Duration defaultTimeout;

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "validateFormula"})
    public Result<Unit> validateFormulaInFcb(String code) {
        return sendAndMap(
                ValidateFormulaRequest.builder().code(code).build(),
                ValidateFormulaResponse.class,
                response -> FcbFormulaMapper.mapValidation(code, response));
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "evaluateFormula"})
    public Result<BigDecimal> evaluateViaFcb(String code, Map<String, BigDecimal> friendlyVars) {
        Map<String, String> aliasValues = FcbFormulaMapper.translateToFcbAliases(friendlyVars);
        return sendAndMap(
                EvaluateFormulaViaFcbRequest.builder()
                        .code(code)
                        .aliasValues(aliasValues)
                        .build(),
                EvaluateFormulaViaFcbResponse.class,
                response -> FcbFormulaMapper.mapEvaluation(code, response));
    }

    private <R extends FcbBaseResponse, T> Result<T> sendAndMap(
            FcbBaseRequest request, Class<R> responseType, Function<R, Result<T>> responseMapper) {

        Result<FcbBaseResponse> result = kafkaClient.sendAndReceive(request, defaultTimeout);
        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }

        FcbBaseResponse raw = result.unwrap();
        if (!responseType.isInstance(raw)) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, request.getOperationName());
        }

        return responseMapper.apply(responseType.cast(raw));
    }
}
