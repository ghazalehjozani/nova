package ir.dotin.loan.trade.config.serialization;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public class RelationTypeKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        try {
            return TradeRelationType.valueOf(key);
        } catch (IllegalArgumentException e) {
            throw new IOException("Cannot deserialize RelationType key: " + key, e);
        }
    }
}
