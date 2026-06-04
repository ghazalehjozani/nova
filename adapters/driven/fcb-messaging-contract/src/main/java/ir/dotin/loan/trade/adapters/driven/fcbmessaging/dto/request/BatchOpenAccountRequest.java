package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public final class BatchOpenAccountRequest extends FcbKafkaBaseRequest {

    @Builder.Default
    @JsonIgnore
    private String operationName = "nova-batch-open-accounts";

    private final List<Item> items;

    @Getter
    @ToString
    @EqualsAndHashCode
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @AllArgsConstructor
    public static final class Item {

        private final String title;
        private final String topicCode;
        private final String branchCode;
        private final String currencyCode;
        private final String transactionId;
    }
}
