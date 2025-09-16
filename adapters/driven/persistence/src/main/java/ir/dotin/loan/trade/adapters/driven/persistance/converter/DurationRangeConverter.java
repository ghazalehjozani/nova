package ir.dotin.loan.trade.adapters.driven.persistance.converter;

import java.time.Duration;

import com.google.common.collect.Range;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.converter.AbstractGenericBidirectionalConverter;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.MissingRequiredFieldException;
import ir.dotin.loan.trade.adapters.driven.persistance.shared.embeddable.DurationRangeEmb;

@Component
public class DurationRangeConverter extends AbstractGenericBidirectionalConverter<Range<Duration>, DurationRangeEmb> {

    public DurationRangeConverter() {
        super(
                ResolvableType.forClassWithGenerics(Range.class, ResolvableType.forClass(Duration.class)),
                ResolvableType.forClass(DurationRangeEmb.class));
    }

    @Override
    @Nullable
    protected DurationRangeEmb convertAToB(
            @Nullable Range<Duration> range, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        if (range == null || !range.hasLowerBound() || !range.hasUpperBound()) {
            return null;
        }

        DurationRangeEmb emb = new DurationRangeEmb();
        emb.setMinDurationDays(range.lowerEndpoint().toDays());
        emb.setMaxDurationDays(range.upperEndpoint().toDays());
        return emb;
    }

    @Override
    @Nullable
    protected Range<Duration> convertBToA(
            @Nullable DurationRangeEmb emb, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        if (emb == null) {
            return null;
        }

        if (emb.getMinDurationDays() == null) {
            throw new MissingRequiredFieldException("minDurationDays");
        }
        if (emb.getMaxDurationDays() == null) {
            throw new MissingRequiredFieldException("maxDurationDays");
        }

        Duration min = Duration.ofDays(emb.getMinDurationDays());
        Duration max = Duration.ofDays(emb.getMaxDurationDays());

        if (min.isNegative()) {
            throw new InvalidDomainStateException("Minimum duration cannot be negative: " + min);
        }
        if (max.isNegative()) {
            throw new InvalidDomainStateException("Maximum duration cannot be negative: " + max);
        }
        if (min.compareTo(max) > 0) {
            throw new InvalidDomainStateException(
                    "Minimum duration cannot be greater than maximum: " + min + " > " + max);
        }

        return Range.closed(min, max);
    }
}
