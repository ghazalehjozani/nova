package ir.dotin.loan.trade.core.application.service.changeguarantor.step;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuaranteePercentage;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.changeguarantor.commandhandler.ChangeGuarantorCommandHandler;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class ChangeGuarantorStepTest {

    private static final UUID FACILITY_ID = UUID.randomUUID();

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    // why: injected into the SUT via @InjectMocks (reflective use EP cannot see)
    @SuppressWarnings("UnusedVariable")
    @Mock
    private Clock clock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanFacility facility;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowContext<ChangeGuarantorCommandHandler.Data> ctx;

    @InjectMocks
    private ChangeGuarantorStep step;

    private static GuarantorParty guarantor(String customerNumber, int percentage) {
        return new GuarantorParty(
                customerNumber,
                PartyType.REAL,
                new CustomerName("Ali", "Ahmadi", null),
                GuaranteePercentage.of(BigDecimal.valueOf(percentage)));
    }

    private ChangeGuarantorCommandHandler.Data data(List<GuarantorParty> guarantors) {
        return new ChangeGuarantorCommandHandler.Data(FACILITY_ID, 0L, guarantors);
    }

    @Test
    void executeChangesGuarantorsAndReturnsEvents() {
        when(ctx.data()).thenReturn(data(List.of(guarantor("111", 100))));
        when(facilityRepository.findById(any())).thenReturn(Optional.of(facility));
        doReturn(Result.success()).when(facility).changeGuarantors(any(), any());
        when(facility.getLoanApplication().getGuarantors()).thenReturn(Set.of(guarantor("111", 100)));
        doReturn(List.<DomainEvent<?>>of()).when(facility).domainEvents();

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(facilityRepository).save(facility, 0L);
    }

    @Test
    void executeFailsWhenFacilityNotFound() {
        when(ctx.data()).thenReturn(data(List.of(guarantor("111", 100))));
        when(facilityRepository.findById(any())).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        verify(facilityRepository, never()).save(any(), anyLong());
    }

    @Test
    void executeFailsWhenPercentagesBelowMinimumAndDoesNotSave() {
        when(ctx.data()).thenReturn(data(List.of(guarantor("111", 40))));
        when(facilityRepository.findById(any())).thenReturn(Optional.of(facility));
        doReturn(Result.success()).when(facility).changeGuarantors(any(), any());
        when(facility.getLoanApplication().getGuarantors()).thenReturn(Set.of(guarantor("111", 40)));

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        verify(facilityRepository, never()).save(any(), anyLong());
    }
}
