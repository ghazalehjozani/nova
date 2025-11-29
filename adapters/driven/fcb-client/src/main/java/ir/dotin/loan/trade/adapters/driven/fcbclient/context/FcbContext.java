package ir.dotin.loan.trade.adapters.driven.fcbclient.context;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcbContext {

    private final Map<String, Object> additionalContext;

    public static FcbContext empty() {
        return FcbContext.builder().additionalContext(new HashMap<>()).build();
    }

    public Object[] toObjectArray() {
        return additionalContext.values().toArray();
    }

    public boolean hasContent() {
        return !additionalContext.isEmpty();
    }
}
