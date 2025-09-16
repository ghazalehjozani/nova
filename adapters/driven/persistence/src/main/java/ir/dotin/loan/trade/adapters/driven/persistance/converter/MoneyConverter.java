package ir.dotin.loan.trade.adapters.driven.persistance.converter;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.convert.converter.AbstractBidirectionalConverter;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.MissingRequiredFieldException;
import ir.dotin.loan.trade.adapters.driven.persistance.shared.embeddable.MoneyEmb;

@Component
public class MoneyConverter extends AbstractBidirectionalConverter<Money, MoneyEmb> {

    public MoneyConverter() {
        super(Money.class, MoneyEmb.class);
    }

    @Override
    @Nullable
    protected MoneyEmb convertAToB(@Nullable Money money) {
        if (money == null) return null;

        MoneyEmb emb = new MoneyEmb();
        emb.setAmount(money.value());
        emb.setCurrency(money.currency().getCode());
        return emb;
    }

    @Override
    @Nullable
    protected Money convertBToA(@Nullable MoneyEmb emb) {
        if (emb == null) return null;

        if (emb.getAmount() == null) {
            throw new MissingRequiredFieldException("amount");
        }
        if (emb.getCurrency() == null) {
            throw new MissingRequiredFieldException("currency");
        }

        CurrencyType currency = CurrencyType.valueOf(emb.getCurrency())
                .orElseThrow(() -> new InvalidDomainStateException("Invalid currency code: " + emb.getCurrency()));

        return Money.valueOf(emb.getAmount(), currency)
                .orElseThrow(() -> new InvalidDomainStateException("Cannot create Money from persisted data"));
    }
}
