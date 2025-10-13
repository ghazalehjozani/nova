package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.NationalCode;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.PersonName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ElectronicBillCustomerDTO;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.IssueDocumentResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.OpenAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.driven.client.CustomerService.CustomerServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceAdapter implements CustomerServicePort {

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;

    @Override
    public Result<CustomerInfo> getCustomerInfo(Party party) {
        Parameter parameter = Parameter.builder()
                .type("constant")
                .key("customerNumbers")
                .value(party.customerNumber())
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
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.CUSTOMER_NOT_FOUND_IN_FCB, party.customerNumber()));
        }

        ElectronicBillCustomerDTO firstCustomerDto = customerData.getFirst();

        return mapToCustomerInfo(firstCustomerDto);
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

    //    @Override
    public Result<IssueDocumentResponse> issueDocument(
            String comment, String item, String itemComment, String branchCode) {

        log.debug(
                "Issuing document with comment: {}, item: {}, itemComment: {}, branchCode: {}",
                comment,
                item,
                itemComment,
                branchCode);

        List<Parameter> parameters = Arrays.asList(
                Parameter.builder().type("String").key("comment").value(comment).build(),
                Parameter.builder().type("String").key("item").value(item).build(),
                Parameter.builder()
                        .type("String")
                        .key("itemComment")
                        .value(itemComment)
                        .build(),
                Parameter.builder()
                        .type("String")
                        .key("branchCode")
                        .value(branchCode)
                        .build());

        Usecases usecases = requestBuilder.buildUseCase("electronic-bill-issue-document", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        return fcbService.executeUsecase(fcbRequest, IssueDocumentResponse.class);
    }

    private Result<CustomerInfo> mapToCustomerInfo(ElectronicBillCustomerDTO customerDto) {
        String[] nameParts =
                customerDto.getName() != null ? customerDto.getName().split(" ", 2) : new String[] {"", ""};
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        PersonName personName = new PersonName(firstName, lastName);
        PartyType partyType = PartyType.valueOf(customerDto.getCustomerType());
        Party party = new Party(customerDto.getCustomerNumber(), partyType, personName);
        Result<NationalCode> nationalCode = NationalCode.valueOf(customerDto.getNationalCode());

        return CustomerInfo.of(party, nationalCode.getValue());
    }
}
