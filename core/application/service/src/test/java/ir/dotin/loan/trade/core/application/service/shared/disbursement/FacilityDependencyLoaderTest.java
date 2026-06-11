package ir.dotin.loan.trade.core.application.service.shared.disbursement;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class FacilityDependencyLoaderTest {

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private TradeLoanTypeRepository loanTypeRepository;

    @Mock
    private TradeLoanArrangementRepository loanArrangementRepository;

    @Mock
    private InstallmentScheduleRepository installmentScheduleRepository;

    @Mock
    private TradeLoanFacility facility;

    @InjectMocks
    private FacilityDependencyLoader loader;

    @Test
    void loadFacilitySucceedsWhenPresent() {
        LoanFacilityId id = LoanFacilityId.of(UUID.randomUUID());
        when(facilityRepository.findById(id)).thenReturn(Optional.of(facility));

        Result<TradeLoanFacility> result = loader.loadFacility(id);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.unwrap()).isSameAs(facility);
    }

    @Test
    void loadFacilityFailsWhenMissing() {
        LoanFacilityId id = LoanFacilityId.of(UUID.randomUUID());
        when(facilityRepository.findById(id)).thenReturn(Optional.empty());

        Result<TradeLoanFacility> result = loader.loadFacility(id);

        assertThat(result.isFailure()).isTrue();
    }
}
