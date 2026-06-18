package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.formula.service.dto.BindingDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.BindingRequest;

public final class BindingRequestMapper {

    private BindingRequestMapper() {}

    public static List<BindingDto> toDtos(@Nullable List<BindingRequest> bindings) {
        if (bindings == null) {
            return List.of();
        }
        return bindings.stream().map(BindingRequestMapper::toDto).toList();
    }

    private static BindingDto toDto(BindingRequest binding) {
        return new BindingDto(
                binding.variable(),
                binding.type(),
                binding.literalValue(),
                binding.fieldName(),
                binding.referencedFormulaId(),
                binding.computedName());
    }
}
