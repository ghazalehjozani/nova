package ir.dotin.loan.trade.adapters.driven.persistance.converter;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.converter.AbstractBidirectionalConverter;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.trade.adapters.driven.persistance.entity.embdeddable.ConfirmTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;

@Component
public class ConfirmTypeConverter extends AbstractBidirectionalConverter<ConfirmType, ConfirmTypeEmb> {

    public ConfirmTypeConverter() {
        super(ConfirmType.class, ConfirmTypeEmb.class);
    }

    @Override
    @Nullable
    protected ConfirmTypeEmb convertAToB(@Nullable ConfirmType confirmType) {
        if (confirmType == null) return null;

        ConfirmTypeEmb emb = new ConfirmTypeEmb();
        emb.setPersonCode(confirmType.personCode());
        emb.setPersonName(confirmType.personName());
        return emb;
    }

    @Override
    @Nullable
    protected ConfirmType convertBToA(@Nullable ConfirmTypeEmb emb) {
        if (emb == null) return null;

        return ConfirmType.of(emb.getPersonCode(), emb.getPersonName())
                .orElseThrow(() -> new InvalidDomainStateException("Cannot create ConfirmType from persisted data"));
    }
}
