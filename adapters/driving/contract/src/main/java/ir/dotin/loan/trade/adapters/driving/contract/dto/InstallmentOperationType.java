package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum InstallmentOperationType {
    INSTALLMENT_COLLECTION("installment.collection"),
    INSTALLMENT_COLLECTION_COMPENSATE("installment.collection.compensate");

    private final String code;

    private static final Map<String, InstallmentOperationType> BY_CODE =
            Arrays.stream(values()).collect(Collectors.toMap(type -> type.code, type -> type));

    public static InstallmentOperationType ofCode(String code) {
        return Optional.ofNullable(BY_CODE.get(code))
                .orElseThrow(
                        () -> new IllegalArgumentException("Unknown or null InstallmentOperationType code: " + code));
    }

    InstallmentOperationType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
