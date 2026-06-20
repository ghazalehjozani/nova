package ir.dotin.loan.trade.core.application.service.changeguarantor.commandhandler;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuaranteePercentage;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ChangeGuarantorCommand;
import ir.dotin.loan.trade.core.application.service.changeguarantor.component.GuarantorResolver;
import ir.dotin.loan.trade.core.application.service.changeguarantor.step.ChangeGuarantorStep;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class ChangeGuarantorCommandHandlerTest {

    @Mock
    private BranchAccessValidator branchAccessValidator;

    @Mock
    private GuarantorResolver guarantorResolver;

    @Mock
    private ChangeGuarantorStep changeGuarantorStep;

    private ChangeGuarantorCommandHandler handler() {
        return new ChangeGuarantorCommandHandler(branchAccessValidator, guarantorResolver, changeGuarantorStep);
    }

    private ChangeGuarantorCommand command(UUID facilityId) {
        return ChangeGuarantorCommand.builder()
                .uid(UUID.randomUUID())
                .version(3L)
                .loanFacilityId(facilityId)
                .branchCode("0101")
                .guarantors(List.of(new ChangeGuarantorCommand.GuarantorInput("111", 100)))
                .build();
    }

    @Test
    void workflowTypeIsStable() {
        assertThat(handler().definition().workflowType()).isEqualTo("change-guarantor");
    }

    @Test
    void seedRunsBranchAccessAndResolvesGuarantors() {
        UUID facilityId = UUID.randomUUID();
        GuarantorParty resolved = new GuarantorParty(
                "111",
                PartyType.REAL,
                new CustomerName("Ali", "Ahmadi", null),
                GuaranteePercentage.of(BigDecimal.valueOf(100)));
        given(branchAccessValidator.verifyCallerCoversFacility(eq("0101"), any(LoanFacilityId.class)))
                .willReturn(Result.success(Unit.INSTANCE));
        given(guarantorResolver.resolve(any())).willReturn(Result.success(List.of(resolved)));

        Result<ChangeGuarantorCommandHandler.Data> result = handler().seed(command(facilityId));

        assertThat(result.isSuccess()).isTrue();
        ChangeGuarantorCommandHandler.Data data = result.unwrap();
        assertThat(data.facilityId()).isEqualTo(facilityId);
        assertThat(data.expectedVersion()).isEqualTo(3L);
        assertThat(data.guarantors()).containsExactly(resolved);
        verify(branchAccessValidator).verifyCallerCoversFacility(eq("0101"), any(LoanFacilityId.class));
        verify(guarantorResolver).resolve(any());
    }
}
