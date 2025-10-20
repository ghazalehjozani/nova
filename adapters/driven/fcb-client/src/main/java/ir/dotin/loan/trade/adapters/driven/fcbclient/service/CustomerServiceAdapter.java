package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.IssueDocumentResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.OpenAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.CustomerMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerService.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceAdapter implements CustomerServicePort {

    private static final String ITEM_SEPARATOR = "#";

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;

    @Override
    public Result<Party> loadCustomerInfo(String customerNumber, CustomerInfoLoadOptions options) {

        log.info("Loading customer info: customerNumber={}, options={}", customerNumber, options);

        Notification inputValidation = validateInputs(customerNumber, options);
        if (inputValidation.hasErrors()) {
            log.error("Input validation failed: {}", inputValidation.getErrorMessages());
            return Result.failure(inputValidation);
        }

        List<Parameter> parameters = buildCustomerInfoParameters(customerNumber, options);

        Usecases usecases = requestBuilder.buildUseCase("load-customer-info", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-customer-info usecase");

        Result<CustomerInfoResponse> fcbResult = fcbService.executeUsecase(fcbRequest, CustomerInfoResponse.class);

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB load-customer-info failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        CustomerInfoResponse fcbResponse = fcbResult.orElseThrow();

        return CustomerMapper.mapToDomainCustomerInfo(fcbResponse);
    }

    private Notification validateInputs(String customerNumber, CustomerInfoLoadOptions options) {
        Notification notification = Notification.create();

        if (customerNumber == null || customerNumber.isBlank()) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Customer number cannot be null or blank");
        }

        if (options == null) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Customer info load options cannot be null");
        }

        return notification;
    }

    private List<Parameter> buildCustomerInfoParameters(String customerNumber, CustomerInfoLoadOptions options) {

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(
                Parameter.builder().key("customerNumber").value(customerNumber).build());

        parameters.add(Parameter.builder()
                .key("sequenceCode")
                .value(options.sequenceCode())
                .build());

        parameters.add(
                Parameter.builder().key("subsystem").value(options.subsystem()).build());

        parameters.add(Parameter.builder()
                .key("includeCapability")
                .value(String.valueOf(options.includeCapability()))
                .build());

        parameters.add(Parameter.builder()
                .key("includeBlackList")
                .value(String.valueOf(options.includeBlackList()))
                .build());

        parameters.add(Parameter.builder()
                .key("includeBaseInfo")
                .value(String.valueOf(options.includeBaseInfo()))
                .build());

        parameters.add(Parameter.builder()
                .key("includeGrayList")
                .value(String.valueOf(options.includeGrayList()))
                .build());

        log.debug("Built customer info parameters for customer: {}", customerNumber);

        return parameters;
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
