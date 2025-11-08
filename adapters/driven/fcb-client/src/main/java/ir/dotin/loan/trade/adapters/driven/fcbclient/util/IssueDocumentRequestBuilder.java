package ir.dotin.loan.trade.adapters.driven.fcbclient.util;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.IssueDocumentRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class IssueDocumentRequestBuilder {

    private static final String SEPARATOR = "#";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Parameter> buildParameters(IssueDocumentRequest request) {
        log.debug("Building FCB parameters for issue-general-document");

        request.validate();

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(Parameter.builder()
                .key("transactionId")
                .value(request.getTransactionId())
                .build());

        parameters.add(
                Parameter.builder().key("comment").value(request.getComment()).build());

        parameters.add(
                Parameter.builder().key("isoCode").value(request.getIsoCode()).build());

        parameters.add(Parameter.builder()
                .key("branchCode")
                .value(request.getBranchCode())
                .build());

        parameters.add(Parameter.builder()
                .key("skipTransferMoneyBillNumber")
                .value(String.valueOf(request.getSkipTransferMoneyBillNumber()))
                .build());

        String itemsValue = String.join(SEPARATOR, request.getItems());
        parameters.add(Parameter.builder().key("items").value(itemsValue).build());

        String itemCommentsValue = String.join(SEPARATOR, request.getItemComments());
        parameters.add(
                Parameter.builder().key("itemComments").value(itemCommentsValue).build());

        if (request.getDocumentExtraInfo() != null
                && !request.getDocumentExtraInfo().isBlank()) {
            parameters.add(Parameter.builder()
                    .key("documentExtraInfo")
                    .value(request.getDocumentExtraInfo())
                    .build());
        }
        log.debug("Built {} parameters for issue-general-document", parameters.size());

        return parameters;
    }
}
