package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentItemDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private DocumentItemTypeDto type;
    private String identifier;
    private TransactionDirectionDto direction;
    private BigDecimal amount;
    private String title;
    private String comment;
    private String soc;
    private String transferMoneyBill;
    private ExtraInfoMetadataDto metadata;
}
