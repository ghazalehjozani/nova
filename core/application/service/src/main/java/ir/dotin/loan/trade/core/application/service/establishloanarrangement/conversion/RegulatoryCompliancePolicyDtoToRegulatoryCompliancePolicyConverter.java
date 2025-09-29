package ir.dotin.loan.trade.core.application.service.establishloanarrangement.conversion;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RegulatoryCompliancePolicy;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;

@Component
public class RegulatoryCompliancePolicyDtoToRegulatoryCompliancePolicyConverter
        implements Converter<
                EstablishTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto,
                Result<RegulatoryCompliancePolicy>> {

    @Override
    public Result<RegulatoryCompliancePolicy> convert(
            EstablishTradeLoanArrangementCommand.RegulatoryCompliancePolicyDto source) {
        return RegulatoryCompliancePolicy.of(
                source.overDuePeriod(), source.deferralPeriod(), source.suspiciousPeriod());
    }
}
