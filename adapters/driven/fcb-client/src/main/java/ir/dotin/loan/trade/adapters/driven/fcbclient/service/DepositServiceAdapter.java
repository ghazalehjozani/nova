package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.Collections;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DepositInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.DepositMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.driven.client.depositservice.DepositServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositServiceAdapter implements DepositServicePort {

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;

    public Result<DepositInfo> getDepositInfo(DepositNumber depositNumber) {
        log.debug("Loading deposit information for: {}", depositNumber);

        Parameter parameter = Parameter.builder()
                .key("depositNumber")
                .value(depositNumber.value())
                .build();

        Usecases usecases = requestBuilder.buildUseCase("load-deposit-by-number", Collections.singletonList(parameter));

        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        Result<DepositInfoResponse> depositInfoResponseResult =
                fcbService.executeUsecase(fcbRequest, DepositInfoResponse.class);

        return DepositMapper.mapToDepositInfo(depositInfoResponseResult.getValue());
    }
}
