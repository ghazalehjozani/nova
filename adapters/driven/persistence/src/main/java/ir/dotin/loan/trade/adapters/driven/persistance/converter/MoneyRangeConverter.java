package ir.dotin.loan.trade.adapters.driven.persistance.converter;

import com.google.common.collect.Range;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.converter.AbstractGenericBidirectionalConverter;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.MissingRequiredFieldException;
import ir.dotin.loan.trade.adapters.driven.persistance.shared.embeddable.AmountRangeEmb;

@Component
public class MoneyRangeConverter extends AbstractGenericBidirectionalConverter<Range<Money>, AmountRangeEmb> {

    public MoneyRangeConverter() {
        super(
                ResolvableType.forClassWithGenerics(Range.class, ResolvableType.forClass(Money.class)),
                ResolvableType.forClass(AmountRangeEmb.class));
    }

    @Override
    @Nullable
    protected AmountRangeEmb convertAToB(
            @Nullable Range<Money> range, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        if (range == null || !range.hasLowerBound() || !range.hasUpperBound()) {
            return null;
        }

        Money lower = range.lowerEndpoint();
        Money upper = range.upperEndpoint();

        if (!lower.currency().equals(upper.currency())) {
            throw new InvalidDomainStateException("Range endpoints have different currencies: "
                    + lower.currency().getCode() + " vs " + upper.currency().getCode());
        }

        AmountRangeEmb emb = new AmountRangeEmb();
        emb.setMinAmount(lower.value());
        emb.setMaxAmount(upper.value());
        emb.setCurrency(lower.currency().getCode());
        return emb;
    }

    @Override
    @Nullable
    protected Range<Money> convertBToA(
            @Nullable AmountRangeEmb emb, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        if (emb == null) return null;

        if (emb.getMinAmount() == null) {
            throw new MissingRequiredFieldException("minAmount");
        }
        if (emb.getMaxAmount() == null) {
            throw new MissingRequiredFieldException("maxAmount");
        }
        if (emb.getCurrency() == null) {
            throw new MissingRequiredFieldException("currency");
        }

        CurrencyType currency = CurrencyType.valueOf(emb.getCurrency())
                .orElseThrow(() -> new InvalidDomainStateException("Invalid currency code: " + emb.getCurrency()));

        Money min = Money.valueOf(emb.getMinAmount(), currency)
                .orElseThrow(() -> new InvalidDomainStateException(
                        "Cannot create minimum Money value from: " + emb.getMinAmount()));

        Money max = Money.valueOf(emb.getMaxAmount(), currency)
                .orElseThrow(() -> new InvalidDomainStateException(
                        "Cannot create maximum Money value from: " + emb.getMaxAmount()));

        return Range.closed(min, max);
    }
}
