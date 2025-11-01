package ir.dotin.loan.trade.adapters.driven.fcbclient.util;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driven.fcbclient.config.FcbConfiguration;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecase;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.resolver.FcbContextResolver;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FcbBaseRequestBuilder {

    private final FcbConfiguration fcbConfiguration;
    private final FcbContextResolver contextResolver;

    public Usecases buildUseCase(String usecaseName, List<Parameter> parameters) {
        Usecase usecase =
                Usecase.builder().name(usecaseName).parameters(parameters).build();

        return Usecases.builder()
                .username(fcbConfiguration.integration().credentials().username())
                .password(fcbConfiguration.integration().credentials().password())
                .ip(contextResolver.resolveIpAddress())
                .currentBranch(contextResolver.resolveCurrentBranch())
                .usecaseList(Collections.singletonList(usecase))
                .build();
    }
}
