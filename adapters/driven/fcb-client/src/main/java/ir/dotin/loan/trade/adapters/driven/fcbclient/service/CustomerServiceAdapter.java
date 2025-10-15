package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ElectronicBillCustomerDTO;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.IssueDocumentResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.OpenAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.CustomerMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.driven.client.customerService.CustomerServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceAdapter implements CustomerServicePort {

    private static final String ITEM_SEPARATOR = "#";

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;

    @Override
    public Result<CustomerInfo> getCustomerInfo(String customerNumber) {
        Parameter parameter = Parameter.builder()
                .type("constant")
                .key("customerNumbers")
                .value(customerNumber)
                .build();

        Usecases usecases =
                requestBuilder.buildUseCase("electronic-bill-customer-info", Collections.singletonList(parameter));
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        Result<CustomerInfoResponse> customerInfoResponseResult =
                fcbService.executeUsecase(fcbRequest, CustomerInfoResponse.class);

        CustomerInfoResponse responseDto = customerInfoResponseResult.value();
        List<ElectronicBillCustomerDTO> customerData =
                requireNonNull(responseDto).getCustomerData();

        if (customerData == null || customerData.isEmpty()) {
            return Result.failure(
                    Notification.ofError(FcbBusinessLocalizedMessageCodes.CUSTOMER_NOT_FOUND_IN_FCB, customerNumber));
        }

        ElectronicBillCustomerDTO firstCustomerDto = customerData.getFirst();

        return CustomerMapper.mapToCustomerInfo(firstCustomerDto);
    }

    @Override
    public Result<AccountId> openAccount(LoanTopic loanTopic, BranchCode branchCode) {
        log.debug(
                "Opening account with title: {}, topicCode: {}, branchCode: {}",
                loanTopic.name(),
                loanTopic.code(),
                branchCode.value());

        List<Parameter> parameters = Arrays.asList(
                Parameter.builder()
                        .type("constant")
                        .key("title")
                        .value(loanTopic.name())
                        .build(),
                Parameter.builder()
                        .type("constant")
                        .key("topicCode")
                        .value(loanTopic.code())
                        .build(),
                Parameter.builder()
                        .type("constant")
                        .key("branchCode")
                        .value(branchCode.value())
                        .build());

        Usecases usecases = requestBuilder.buildUseCase("electronic-bill-create-account", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        Result<OpenAccountResponse> openAccountResponseResult =
                fcbService.executeUsecase(fcbRequest, OpenAccountResponse.class);

        if (Objects.isNull(openAccountResponseResult.value())
                || Objects.isNull(openAccountResponseResult.value().getAccountNumber())) {
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.ACCOUNT_NUMBER_NOT_FOUND,
                    openAccountResponseResult.value().getAccountNumber()));
        }
        return AccountId.valueOf(openAccountResponseResult.value().getAccountNumber());
    }

    @Override
    public Result<TransactionNumber> issueDocument(LoanTransaction loanTransaction) {

        log.info(
                "Starting document issuance for loan facility: {}",
                loanTransaction.loanFacilityId().value());

        Notification validationNotification = loanTransaction.validate();
        if (validationNotification.hasErrors()) {
            log.error("LoanTransaction validation failed: {}", validationNotification.getErrorMessages());
            return Result.failure(validationNotification);
        }

        Result<List<String>> itemsResult = CustomerMapper.mapToFcbItems(loanTransaction);
        if (itemsResult.isFailure()) {
            log.error(
                    "Failed to map articles to FCB items: {}",
                    itemsResult.notification().getErrorMessages());
            return Result.failure(itemsResult.notification());
        }
        List<String> items = itemsResult.orElseThrow();

        List<String> itemComments = CustomerMapper.mapToFcbItemComments(loanTransaction);

        if (items.size() != itemComments.size()) {
            log.error("Items count ({}) doesn't match comments count ({})", items.size(), itemComments.size());
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE, "Items and comments count mismatch"));
        }

        String branchCode = CustomerMapper.extractBranchCode(loanTransaction);
        String documentComment = CustomerMapper.extractDocumentComment(loanTransaction);

        List<Parameter> parameters = buildFcbIssueDocumentParameters(documentComment, items, itemComments, branchCode);

        Usecases usecases = requestBuilder.buildUseCase("electronic-bill-issue-document", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB document issuance with {} items", items.size());

        Result<IssueDocumentResponse> fcbResult = fcbService.executeUsecase(fcbRequest, IssueDocumentResponse.class);

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB document issuance failed: {}", fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        return TransactionNumber.of(fcbResult.value().getTransaction());
    }

    private List<Parameter> buildFcbIssueDocumentParameters(
            String documentComment, List<String> items, List<String> itemComments, String branchCode) {

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(Parameter.builder()
                .type("String")
                .key("comment")
                .value(documentComment)
                .build());

        String itemsValue = String.join(ITEM_SEPARATOR, items);
        parameters.add(
                Parameter.builder().type("String").key("item").value(itemsValue).build());

        String itemCommentsValue = String.join(ITEM_SEPARATOR, itemComments);
        parameters.add(Parameter.builder()
                .type("String")
                .key("itemComment")
                .value(itemCommentsValue)
                .build());

        parameters.add(Parameter.builder()
                .type("String")
                .key("branchCode")
                .value(branchCode)
                .build());

        log.debug(
                "Built FCB parameters - comment: {}, items: {}, itemComments: {}, branch: {}",
                documentComment,
                itemsValue,
                itemCommentsValue,
                branchCode);

        return parameters;
    }
}
