package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.platform.formula.service.cqrs.command.UpdateFormulaCommand;
import ir.dotin.loan.trade.adapters.driving.contract.dto.UpdateFormulaRequest;

@Component
public class UpdateFormulaRequestToCommandMapper {

    public UpdateFormulaCommand toCommand(String code, UpdateFormulaRequest request, UUID uid) {
        return new UpdateFormulaCommand(
                uid,
                null,
                code,
                request.expression(),
                request.description(),
                BindingRequestMapper.toDtos(request.bindings()));
    }
}
