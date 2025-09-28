package ir.dotin.loan.trade.adapters.driving.web.mapper;

import java.time.Period;
import java.util.UUID;

import com.google.common.collect.Range;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.trade.adapters.driving.web.controller.dto.EstablishTradeLoanArrangementRequest;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {UUID.class, Range.class, Period.class})
public interface EstablishArrangementRequestToCommandMapper {

    EstablishTradeLoanArrangementCommand toCommand(EstablishTradeLoanArrangementRequest request);

    default Range<Money> map(EstablishTradeLoanArrangementRequest.AmountRangeDto amountRangeDto) {
        if (amountRangeDto == null) {
            return null;
        }
        Money minMoney =
                Money.valueOf(amountRangeDto.minAmount(), CurrencyType.IRR).orElseThrow();
        Money maxMoney =
                Money.valueOf(amountRangeDto.maxAmount(), CurrencyType.IRR).orElseThrow();
        return Range.closed(minMoney, maxMoney);
    }

    default Range<LoanDuration> map(EstablishTradeLoanArrangementRequest.DurationRangeDto durationRangeDto) {
        if (durationRangeDto == null) {
            return null;
        }
        LoanDuration minDuration = LoanDuration.of(Period.ofDays(durationRangeDto.minDurationDays()))
                .orElseThrow();
        LoanDuration maxDuration = LoanDuration.of(Period.ofDays(durationRangeDto.maxDurationDays()))
                .orElseThrow();
        return Range.closed(minDuration, maxDuration);
    }
}
