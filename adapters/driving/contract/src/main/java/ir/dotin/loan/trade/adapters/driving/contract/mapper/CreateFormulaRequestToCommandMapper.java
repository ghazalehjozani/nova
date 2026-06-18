package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.platform.formula.service.cqrs.command.CreateFormulaCommand;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CreateFormulaRequest;

@Component
public class CreateFormulaRequestToCommandMapper {

    public CreateFormulaCommand toCommand(CreateFormulaRequest request, UUID uid) {
        return new CreateFormulaCommand(
                uid,
                null,
                request.code(),
                request.expression(),
                request.description(),
                BindingRequestMapper.toDtos(request.bindings()));
    }
}
