package ir.dotin.loan.trade.e2e.rest.facility;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.adapters.driving.contract.dto.ApproveFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DisburseDestinationRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.IssueFacilityContractRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.LumpSumDisbursementRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest.InstallmentSchedulePlanDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest.InstallmentSpecDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest.LoanApplicationDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest.SamatDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.PartyRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.SubmitFacilityForApprovalRequest;
import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator.MinimalChain;

import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.assertSuccess;
import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.extractData;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class CompleteFacilityLifecycleRestE2ETest extends AbstractRestE2E {

    private String loanTypeCode;
    private String arrangementCode;

    @BeforeAll
    void setupFixtures() {
        MinimalChain chain = prerequisiteOrchestrator.createMinimalChain();
        loanTypeCode = chain.loanType().getCode().getValue();
        arrangementCode = chain.arrangement().getCode();
    }

    @Test
    void shouldCompleteFacilityLifecycleViaRest() throws Exception {
        // Step 1: Open facility case
        OriginateLoanFacilityRequest openRequest = buildOpenCaseRequest();
        ResponseEntity<String> openResponse = postJson("/facilities/open-case", openRequest);
        assertSuccess(openResponse, objectMapper);

        JsonNode openData = extractData(openResponse, objectMapper);
        // Extract facility ID from the event stream data
        String facilityId = extractFacilityIdFromEvents(openData);
        assertThat(facilityId)
                .as("Facility ID should be extractable from open-case response")
                .isNotNull();

        // Step 2: Submit for approval
        SubmitFacilityForApprovalRequest submitRequest =
                new SubmitFacilityForApprovalRequest(UUID.randomUUID(), 1, Map.of());
        ResponseEntity<String> submitResponse =
                postJson("/facilities/" + facilityId + "/submit-for-approval", submitRequest);
        assertSuccess(submitResponse, objectMapper);

        // Step 3: Approve
        ApproveFacilityRequest approveRequest = new ApproveFacilityRequest(2L, "1", Map.of());
        ResponseEntity<String> approveResponse = postJson("/facilities/" + facilityId + "/approve", approveRequest);
        assertSuccess(approveResponse, objectMapper);

        // Step 4: Issue contract
        IssueFacilityContractRequest contractRequest = new IssueFacilityContractRequest(
                UUID.randomUUID(),
                3L,
                Map.of(
                        "terminalId",
                        "E2E",
                        "terminalType",
                        "WEB",
                        "channel",
                        "INTERNET_BANK",
                        "toolSource",
                        "CORE",
                        "productCode",
                        "LOAN",
                        "networkType",
                        "INTERNET"));
        ResponseEntity<String> contractResponse =
                postJson("/facilities/" + facilityId + "/issue-contract", contractRequest);
        assertSuccess(contractResponse, objectMapper);

        // Step 5: Disburse (lump sum)
        LumpSumDisbursementRequest disburseRequest = new LumpSumDisbursementRequest(4L, LocalDate.now(), Map.of());
        ResponseEntity<String> disburseResponse =
                postJson("/facilities/" + facilityId + "/disburse/lump-sum", disburseRequest);
        assertSuccess(disburseResponse, objectMapper);

        // Step 6: Verify via query
        ResponseEntity<String> queryResponse = getJson("/loan-facilities/" + facilityId);
        assertSuccess(queryResponse, objectMapper);
        JsonNode queryData = extractData(queryResponse, objectMapper);
        assertThat(queryData).isNotNull();
    }

    private String extractFacilityIdFromEvents(JsonNode data) {
        // The data is an EventStream containing domain events
        // Try to extract the facility ID from the event data
        if (data == null) {
            return null;
        }
        // EventStream may contain events as an array
        if (data.isArray()) {
            for (JsonNode event : data) {
                String id = findFacilityIdInNode(event);
                if (id != null) return id;
            }
        }
        // Or it may be a wrapper with events field
        JsonNode events = data.get("events");
        if (events != null && events.isArray()) {
            for (JsonNode event : events) {
                String id = findFacilityIdInNode(event);
                if (id != null) return id;
            }
        }
        // Try direct field access
        String id = findFacilityIdInNode(data);
        if (id != null) return id;
        // Fallback: search the entire JSON tree
        return findFacilityIdDeep(data);
    }

    private String findFacilityIdInNode(JsonNode node) {
        for (String field : List.of("facilityId", "loanFacilityId", "aggregateId", "id")) {
            JsonNode value = node.get(field);
            if (value != null && !value.isNull() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return null;
    }

    private String findFacilityIdDeep(JsonNode node) {
        if (node == null) return null;
        if (node.isObject()) {
            for (String field : List.of("facilityId", "loanFacilityId", "aggregateId")) {
                if (node.has(field) && !node.get(field).isNull()) {
                    return node.get(field).asText();
                }
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                String result = findFacilityIdDeep(entry.getValue());
                if (result != null) return result;
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                String result = findFacilityIdDeep(child);
                if (result != null) return result;
            }
        }
        return null;
    }

    private OriginateLoanFacilityRequest buildOpenCaseRequest() {
        return new OriginateLoanFacilityRequest(
                loanTypeCode,
                arrangementCode,
                new LoanApplicationDto(
                        Instant.now(),
                        Set.of(new PartyRequestDto.ApplicantDto("12345678", Map.of())),
                        new BigDecimal("50000000"),
                        DisbursementMethod.LUMP_SUM,
                        "IRR",
                        12,
                        ApplicantChannel.DIGITAL_BANK,
                        10,
                        3,
                        new DisburseDestinationRequestDto.DepositDestinationDto("1.10.1357.60", Map.of()),
                        "2-1",
                        "0",
                        "03",
                        "E2E full lifecycle test",
                        null,
                        "A",
                        new SamatDto("1234567899876543", null, null, null, null, null)),
                new InstallmentSchedulePlanDto(List.of(
                        new InstallmentSpecDto(
                                1,
                                LocalDate.now().plusMonths(1),
                                new BigDecimal("16666667"),
                                new BigDecimal("750000"),
                                null,
                                null),
                        new InstallmentSpecDto(
                                2,
                                LocalDate.now().plusMonths(2),
                                new BigDecimal("16666667"),
                                new BigDecimal("625000"),
                                null,
                                null),
                        new InstallmentSpecDto(
                                3,
                                LocalDate.now().plusMonths(3),
                                new BigDecimal("16666666"),
                                new BigDecimal("500000"),
                                null,
                                null))),
                Map.of());
    }
}
