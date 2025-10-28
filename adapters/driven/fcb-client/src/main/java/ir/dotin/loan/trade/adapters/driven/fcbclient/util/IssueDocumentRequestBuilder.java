package ir.dotin.loan.trade.adapters.driven.fcbclient.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
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

        addParameter(parameters, "transactionId", request.getTransactionId());

        addParameter(parameters, "comment", request.getComment());
        addParameter(parameters, "isoCode", request.getIsoCode());
        addParameter(parameters, "branchCode", request.getBranchCode());

        if (request.getTransferMoneyBillNumber() != null) {
            addParameter(parameters, "transferMoneyBillNumber", request.getTransferMoneyBillNumber());
        }

        if (request.getDocumentTemplateCode() != null) {
            addParameter(parameters, "documentTemplateCode", request.getDocumentTemplateCode());
        }

        addParameter(
                parameters, "skipTransferMoneyBillNumber", String.valueOf(request.getSkipTransferMoneyBillNumber()));

        if (request.getTemplateObject() != null && !request.getTemplateObject().isEmpty()) {
            String templateJson = convertMapToEscapedJson(request.getTemplateObject());
            addParameter(parameters, "templateObject", templateJson);
        }

        addParameter(parameters, "items", joinWithSeparator(request.getItems()));
        addParameter(parameters, "itemComments", joinWithSeparator(request.getItemComments()));

        if (request.getTransferMoneyBills() != null
                && !request.getTransferMoneyBills().isEmpty()) {
            addParameter(parameters, "transferMoneyBills", joinWithSeparator(request.getTransferMoneyBills()));
        }

        if (request.getItemSOCs() != null && !request.getItemSOCs().isEmpty()) {
            addParameter(parameters, "itemSOCs", joinWithSeparator(request.getItemSOCs()));
        }

        if (request.getDocumentExtraInfo() != null) {
            String extraInfoJson = convertObjectToEscapedJson(request.getDocumentExtraInfo());
            addParameter(parameters, "documentExtraInfo", extraInfoJson);
        }

        if (request.getDocumentItemExtraInfoList() != null
                && !request.getDocumentItemExtraInfoList().isEmpty()) {
            String itemExtraInfoList = request.getDocumentItemExtraInfoList().stream()
                    .map(this::convertObjectToEscapedJson)
                    .collect(Collectors.joining(SEPARATOR));
            addParameter(parameters, "documentItemExtraInfoList", itemExtraInfoList);
        }

        log.debug("Built {} parameters for issue-general-document", parameters.size());
        return parameters;
    }

    private void addParameter(List<Parameter> parameters, String key, String value) {
        if (value != null) {
            parameters.add(Parameter.builder().key(key).value(value).build());
        }
    }

    private String joinWithSeparator(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(SEPARATOR, list);
    }

    private String convertMapToEscapedJson(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return json.replace("\"", "&quot;");
        } catch (JsonProcessingException e) {
            log.error("Error converting map to JSON", e);
            return "{}";
        }
    }

    private String convertObjectToEscapedJson(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return json.replace("\"", "&quot;");
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON", e);
            return "{}";
        }
    }
}
