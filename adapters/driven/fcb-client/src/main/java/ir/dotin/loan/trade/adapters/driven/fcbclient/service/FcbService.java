package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbclient.context.FcbContext;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;

public interface FcbService {

    /** Execute FCB use case with context */
    <T> Result<T> executeUsecase(FcbRequest request, Class<T> responseClass, FcbContext context);
}
