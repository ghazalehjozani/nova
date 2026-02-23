package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class EconomicSectorKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String code;
    private @Nullable String name;
    private @Nullable Boolean hasChild;
    private @Nullable String parentCode;

    public @Nullable String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public @Nullable Boolean getHasChild() {
        return hasChild;
    }

    public void setHasChild(@Nullable Boolean hasChild) {
        this.hasChild = hasChild;
    }

    public @Nullable String getParentCode() {
        return parentCode;
    }

    public void setParentCode(@Nullable String parentCode) {
        this.parentCode = parentCode;
    }
}
