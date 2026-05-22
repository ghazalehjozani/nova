package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.ValueObject;

public record BranchDetails(
        String code,
        String name,
        String foreignName,
        Long globalCode,
        String managerName,
        String samCode,
        String swiftCode,
        String clearBranch,
        String cityCode,
        String bankCode)
        implements ValueObject<BranchDetails> {

    public static Result<BranchDetails> of(
            String code,
            String name,
            String foreignName,
            Long globalCode,
            String managerName,
            String samCode,
            String swiftCode,
            String clearBranch,
            String cityCode,
            String bankCode) {

        BranchDetails type = new BranchDetails(
                code, name, foreignName, globalCode, managerName, samCode, swiftCode, clearBranch, cityCode, bankCode);

        Notification internalValidation = type.validate();
        if (internalValidation.hasErrors()) {
            return Result.failure(internalValidation);
        }
        return Result.success(type);
    }
}
