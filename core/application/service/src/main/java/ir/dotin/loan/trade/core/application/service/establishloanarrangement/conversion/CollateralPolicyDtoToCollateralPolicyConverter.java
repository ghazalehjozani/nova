package ir.dotin.loan.trade.core.application.service.establishloanarrangement.conversion;

import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.CollateralPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.CollateralType;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;

import static java.util.Objects.requireNonNull;

@Component
public class CollateralPolicyDtoToCollateralPolicyConverter
        implements Converter<EstablishTradeLoanArrangementCommand.CollateralPolicyDto, Result<CollateralPolicy>> {

    @Override
    public Result<CollateralPolicy> convert(EstablishTradeLoanArrangementCommand.CollateralPolicyDto source) {
        List<CollateralType> collateralTypes = source.collateralTypes().stream()
                .map(dto -> {
                    Result<CollateralType> result = CollateralType.of(dto.code(), dto.name());
                    return result.value();
                })
                .toList();
        return CollateralPolicy.of(requireNonNull(collateralTypes), source.totalPercent());
    }
}
