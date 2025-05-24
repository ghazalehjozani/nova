package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loantype.enums.SegmentType;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanApplicationStatus;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanTopicConfiguration;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.*;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

public record TradeLoanTypeCreated(UUID eventId, TradeLoanTypeId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvent<TradeLoanTypeCreated, TradeLoanTypeCreated.Payload> {

    public TradeLoanTypeCreated {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(aggregateId);
        Objects.requireNonNull(payload);
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
            Set<TradeLoanArrangementId> loanArrangementIds,
            boolean active) {

        public Payload {
            Objects.requireNonNull(code);
            Objects.requireNonNull(title);
            Objects.requireNonNull(gatewayType);
            Objects.requireNonNull(loanApplicationAllowed);
            Objects.requireNonNull(segmentType);
            Objects.requireNonNull(economicSectors);
            Objects.requireNonNull(loanTopicAssignments);
            Objects.requireNonNull(incomeIds);
            Objects.requireNonNull(attributes);
            Objects.requireNonNull(groupId);
            Objects.requireNonNull(loanArrangementIds);
        }
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CREATED";
    }
}
