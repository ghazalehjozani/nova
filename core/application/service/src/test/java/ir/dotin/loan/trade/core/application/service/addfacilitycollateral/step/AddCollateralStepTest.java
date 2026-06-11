package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.step;

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
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.AbstractCollateralValidationService;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow.CollateralData;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class AddCollateralStepTest {

    private static final UUID FACILITY_ID = UUID.randomUUID();
    private static final UUID REQUEST_ID = UUID.randomUUID();

    @Mock
    private AddFacilityCollateralCommandMapper mapper;

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private TradeLoanArrangementRepository arrangementRepository;

    @Mock
    private TradeLoanFacilityService domainService;

    @Mock
    private AbstractCollateralValidationService collateralValidationService;

    @Mock
    private Clock clock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanFacility facility;

    @Mock
    private TradeLoanArrangement arrangement;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowContext<CollateralData> ctx;

    @InjectMocks
    private AddCollateralStep step;

    private CollateralData data() {
        return CollateralData.initial(FACILITY_ID, REQUEST_ID, List.of(), 0L);
    }

    @Test
    void executeAddsCollateralAndReturnsEvents() {
        when(ctx.data()).thenReturn(data());
        when(facilityRepository.findById(any())).thenReturn(Optional.of(facility));
        when(arrangementRepository.findById(any())).thenReturn(Optional.of(arrangement));
        when(mapper.toCollaterals(any())).thenReturn(List.of());
        when(domainService.addCollateral(any(), any())).thenReturn(Result.success());
        when(collateralValidationService.validateFacilityCollaterals(any(), any()))
                .thenReturn(Result.success(Boolean.TRUE));
        doReturn(List.<DomainEvent<?>>of()).when(facility).domainEvents();

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(facilityRepository).save(facility, 0L);
    }

    @Test
    void executeFailsWhenFacilityNotFound() {
        when(ctx.data()).thenReturn(data());
        when(facilityRepository.findById(any())).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        verifyNoInteractions(domainService);
    }

    @Test
    void compensateRevertsAndSaves() {
        when(ctx.data()).thenReturn(data());
        when(facilityRepository.findById(any())).thenReturn(Optional.of(facility));
        doReturn(Result.success()).when(facility).revertAddCollateral(any(), any());

        StepResult<Void> result = step.compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(facilityRepository).save(facility);
    }

    @Test
    void compensateFailsWhenFacilityNotFound() {
        when(ctx.data()).thenReturn(data());
        when(facilityRepository.findById(any())).thenReturn(Optional.empty());

        StepResult<Void> result = step.compensate(ctx);

        assertThat(result.isFailure()).isTrue();
        verify(facilityRepository, never()).save(any());
    }
}
