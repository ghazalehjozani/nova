package ir.dotin.loan.trade.core.domain.loantype.entity;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loantype.entity.AbstractLoanType;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.EconomicSectorCurrency;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanApplicationStatus;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeGroupAssigned;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeGroupRemoved;

import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeLoanType group membership")
@SuppressWarnings("NullAway")
final class TradeLoanTypeGroupAssignmentTest {

    @Mock
    private LoanTypeCode mockLoanTypeCode;

    @Mock
    private Title mockTitle;

    @Mock
    private LoanApplicationStatus mockLoanApplicationStatus;

    @Mock
    private EconomicSectorCurrency mockEconomicSector;

    private Set<LoanArrangementId> validArrangementIds;
    private Clock clock;

    @BeforeEach
    void setUp() {
        var arrangementId1 = LoanArrangementId.of(randomUUID());
        var arrangementId2 = LoanArrangementId.of(randomUUID());
        validArrangementIds = ImmutableSet.of(arrangementId1, arrangementId2);
        clock = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), UTC);
    }

    @Test
    @DisplayName("assignToGroup sets group and registers event")
    void assignToGroupSetsGroupAndRegistersEvent() {
        TradeLoanType loanType = TradeLoanType.reconstitute(validBuilder());
        LoanTypeGroupId group = LoanTypeGroupId.generate();

        Result<? extends AbstractLoanType> result = loanType.assignToGroup(group, clock);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.unwrap().getGroupId()).isEqualTo(group);
        assertThat(loanType.domainEvents()).anyMatch(event -> event instanceof TradeLoanTypeGroupAssigned);
    }

    @Test
    @DisplayName("removeFromGroup clears group and registers event")
    void removeFromGroupClearsGroupAndRegistersEvent() {
        TradeLoanType loanType = TradeLoanType.reconstitute(validBuilder().groupId(LoanTypeGroupId.generate()));

        Result<? extends AbstractLoanType> result = loanType.removeFromGroup(clock);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.unwrap().getGroupId()).isNull();
        assertThat(loanType.domainEvents()).anyMatch(event -> event instanceof TradeLoanTypeGroupRemoved);
    }

    private TradeLoanType.Builder validBuilder() {
        return TradeLoanType.builder()
                .id(LoanTypeId.of(randomUUID()))
                .code(mockLoanTypeCode)
                .title(mockTitle)
                .gatewayType(GatewayType.LOAN)
                .loanApplicationAllowed(mockLoanApplicationStatus)
                .economicSectorCurrencies(ImmutableSet.of(mockEconomicSector))
                .loanArrangementIds(validArrangementIds);
    }
}
