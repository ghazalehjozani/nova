package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loantype.enums.SegmentType;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanApplicationStatus;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanTopicConfiguration;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;

public record TradeLoanTypeCreated(UUID eventId, LoanTypeId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvent<TradeLoanTypeCreated, TradeLoanTypeCreated.Payload> {

    public TradeLoanTypeCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
    }

    public record Payload(
            LoanTypeCode code,
            Title title,
            GatewayType gatewayType,
            LoanApplicationStatus loanApplicationAllowed,
            SegmentType segmentType,
            Set<EconomicSector> economicSectors,
            Set<LoanTopicConfiguration<?>> loanTopicAssignments,
            Set<IncomeId> incomeIds,
            List<Attribute> attributes,
            LoanTypeGroupId groupId,
            Set<LoanArrangementId> loanArrangementIds,
            boolean active) {

        public Payload {
            requireNonNull(code);
            requireNonNull(title);
            requireNonNull(gatewayType);
            requireNonNull(loanApplicationAllowed);
            requireNonNull(segmentType);
            requireNonNull(economicSectors);
            requireNonNull(loanTopicAssignments);
            requireNonNull(incomeIds);
            requireNonNull(attributes);
            requireNonNull(groupId);
            requireNonNull(loanArrangementIds);
        }
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CREATED";
    }
}
