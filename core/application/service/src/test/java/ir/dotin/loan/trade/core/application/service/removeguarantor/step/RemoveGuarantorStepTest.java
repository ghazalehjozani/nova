package ir.dotin.loan.trade.core.application.service.removeguarantor.step;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
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
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.removeguarantor.commandhandler.RemoveGuarantorCommandHandler;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class RemoveGuarantorStepTest {

    private static final UUID FACILITY_ID = UUID.randomUUID();
    private static final String CUSTOMER_NUMBER = "CUST001";

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private TradeLoanArrangementRepository arrangementRepository;

    // why: injected into the SUT via @InjectMocks (reflective use EP cannot see)
    @SuppressWarnings("UnusedVariable")
    @Mock
    private Clock clock;

    @Mock
    private TradeLoanFacility facility;

    @Mock
    private TradeLoanArrangement arrangement;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowContext<RemoveGuarantorCommandHandler.Data> ctx;

    @InjectMocks
    private RemoveGuarantorStep step;

    private RemoveGuarantorCommandHandler.Data data() {
        return new RemoveGuarantorCommandHandler.Data(FACILITY_ID, 0L, CUSTOMER_NUMBER);
    }

    @Test
    void executeRemovesGuarantorUsingArrangementMinimumAndReturnsEvents() {
        when(ctx.data()).thenReturn(data());
        when(facilityRepository.findById(any())).thenReturn(Optional.of(facility));
        when(arrangementRepository.findById(any())).thenReturn(Optional.of(arrangement));
        when(arrangement.getGuarantorCount()).thenReturn(1);
        when(facility.removeGuarantor(eq(CUSTOMER_NUMBER), eq(1), any())).thenReturn(Result.success());
        doReturn(List.<DomainEvent<?>>of()).when(facility).domainEvents();

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(facility).removeGuarantor(eq(CUSTOMER_NUMBER), eq(1), any());
        verify(facilityRepository).save(facility, 0L);
    }

    @Test
    void executeTreatsNullArrangementGuarantorCountAsZeroMinimum() {
        when(ctx.data()).thenReturn(data());
        when(facilityRepository.findById(any())).thenReturn(Optional.of(facility));
        when(arrangementRepository.findById(any())).thenReturn(Optional.of(arrangement));
        when(arrangement.getGuarantorCount()).thenReturn(null);
        when(facility.removeGuarantor(eq(CUSTOMER_NUMBER), eq(0), any())).thenReturn(Result.success());
        doReturn(List.<DomainEvent<?>>of()).when(facility).domainEvents();

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(facility).removeGuarantor(eq(CUSTOMER_NUMBER), eq(0), any());
    }

    @Test
    void executeFailsWhenFacilityNotFound() {
        when(ctx.data()).thenReturn(data());
        when(facilityRepository.findById(any())).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        verify(facilityRepository).findById(any());
        verifyNoMoreInteractions(facilityRepository);
    }
}
