package ir.dotin.loan.trade.adapters.driven.fcbclient.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcbContext {

    private final String depositNumber;
    private final String economicSectorCode;
    private final String loanTypeCode;
    private final String reasonTypeCode;
    private final String resourceCode;
    private final String currencyCode;
    private final Map<String, Object> additionalContext;

    public static FcbContext empty() {
        return FcbContext.builder().additionalContext(new HashMap<>()).build();
    }

    public FcbContext withAdditional(String key, Object value) {
        Map<String, Object> newAdditional = new HashMap<>(this.additionalContext);
        newAdditional.put(key, value);

        return FcbContext.builder()
                .depositNumber(this.depositNumber)
                .economicSectorCode(this.economicSectorCode)
                .reasonTypeCode(this.reasonTypeCode)
                .resourceCode(this.resourceCode)
                .loanTypeCode(this.loanTypeCode)
                .currencyCode(this.currencyCode)
                .additionalContext(newAdditional)
                .build();
    }

    public Optional<Object> getAdditional(String key) {
        return Optional.ofNullable(additionalContext.get(key));
    }

    public Object[] toObjectArray() {
        java.util.List<Object> values = new java.util.ArrayList<>();

        if (depositNumber != null) {
            values.add(depositNumber);
        }
        if (economicSectorCode != null) {
            values.add(economicSectorCode);
        }
        if (reasonTypeCode != null) {
            values.add(reasonTypeCode);
        }
        if (resourceCode != null) {
            values.add(resourceCode);
        }
        if (loanTypeCode != null) {
            values.add(loanTypeCode);
        }
        if (currencyCode != null) {
            values.add(currencyCode);
        }

        return values.toArray();
    }

    public boolean hasContent() {
        return depositNumber != null
                || economicSectorCode != null
                || reasonTypeCode != null
                || resourceCode != null
                || loanTypeCode != null
                || currencyCode != null
                || !additionalContext.isEmpty();
    }
}
