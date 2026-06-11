package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.step;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralReservationReleaser;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow.CollateralData;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class ReserveCollateralsStepTest {

    private static final UUID FACILITY_ID = UUID.randomUUID();
    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final String SERIAL = "SER-1";

    @Mock
    private AddFacilityCollateralCommandMapper mapper;

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private CollateralServicePort collateralServicePort;

    @Mock
    private CollateralReservationReleaser collateralReservationReleaser;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanFacility facility;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowContext<CollateralData> ctx;

    @InjectMocks
    private ReserveCollateralsStep step;

    private CollateralData data() {
        return CollateralData.initial(FACILITY_ID, REQUEST_ID, List.of(), 0L);
    }

    private Collateral collateral() {
        Money amount = Money.valueOf(100L, CurrencyType.valueOf("IRR").unwrap()).unwrap();
        return Collateral.valueOf(
                        CollateralType.CHEQUE,
                        "desc",
                        CollateralSerial.of(SERIAL).unwrap(),
                        amount)
                .unwrap();
    }

    @Test
    void executeReservesAllCollateralsAndSucceeds() {
        when(ctx.data()).thenReturn(data());
        when(facilityRepository.findById(any())).thenReturn(Optional.of(facility));
        when(facility.getLoanApplication().getApplicationNumber())
                .thenReturn(Optional.of(org.mockito.Mockito.mock(ApplicationNumber.class)));
        when(mapper.toCollaterals(any())).thenReturn(List.of(collateral()));
        when(collateralServicePort.reserveCollateral(any(), any(), any(), anyInt(), any()))
                .thenReturn(Result.success(List.of(CollateralSerial.of(SERIAL).unwrap())));

        StepResult<Void> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(collateralReservationReleaser, never()).release(any(), any(), any());
    }

    @Test
    void executeFailsWhenFacilityNotFound() {
        when(ctx.data()).thenReturn(data());
        when(facilityRepository.findById(any())).thenReturn(Optional.empty());

        StepResult<Void> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        verifyNoInteractions(collateralServicePort);
    }

    @Test
    void executeFailsAndReleasesWhenReservationFails() {
        when(ctx.data()).thenReturn(data());
        when(facilityRepository.findById(any())).thenReturn(Optional.of(facility));
        when(facility.getLoanApplication().getApplicationNumber())
                .thenReturn(Optional.of(org.mockito.Mockito.mock(ApplicationNumber.class)));
        when(mapper.toCollaterals(any())).thenReturn(List.of(collateral()));
        when(collateralServicePort.reserveCollateral(any(), any(), any(), anyInt(), any()))
                .thenReturn(Result.failure(
                        Notification.ofError(TradeLoanApplicationServiceErrors.COLLATERAL_VALIDATION_FAILED, "x")));

        StepResult<Void> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        verify(collateralReservationReleaser).release(any(), any(), eq(REQUEST_ID));
    }

    @Test
    void compensateReleasesReservedSerials() {
        CollateralData withReserved = data().withReservedSerials(List.of(SERIAL));
        when(ctx.data()).thenReturn(withReserved);
        when(facilityRepository.findById(any())).thenReturn(Optional.of(facility));
        when(facility.getLoanApplication().getApplicationNumber())
                .thenReturn(Optional.of(org.mockito.Mockito.mock(ApplicationNumber.class)));

        StepResult<Void> result = step.compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<List<String>> serials = ArgumentCaptor.forClass(List.class);
        verify(collateralReservationReleaser).release(any(), serials.capture(), eq(REQUEST_ID));
        assertThat(serials.getValue()).containsExactly(SERIAL);
    }

    @Test
    void compensateSkipsWhenNoApplicationNumber() {
        when(ctx.data()).thenReturn(data().withReservedSerials(List.of(SERIAL)));
        when(facilityRepository.findById(any())).thenReturn(Optional.of(facility));
        when(facility.getLoanApplication().getApplicationNumber()).thenReturn(Optional.empty());

        StepResult<Void> result = step.compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(collateralReservationReleaser, never()).release(any(), any(), any());
    }
}
