package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;

public interface FcbService {

    <T> Result<T> executeUsecase(FcbRequest request, Class<T> responseClass, Object... contextArgs);
}
