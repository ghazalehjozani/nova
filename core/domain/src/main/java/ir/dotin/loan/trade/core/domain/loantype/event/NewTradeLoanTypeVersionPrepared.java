package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loantype.enums.SegmentType;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanApplicationStatus;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanTopicConfiguration;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;

public record NewTradeLoanTypeVersionPrepared(UUID eventId, LoanTypeId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanTypeEvent<NewTradeLoanTypeVersionPrepared, NewTradeLoanTypeVersionPrepared.Payload> {

    public NewTradeLoanTypeVersionPrepared {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
        if (!aggregateId.equals(payload.newAggregateId()))
            throw new IllegalArgumentException("aggregateId must match payload.newAggregateId");
    }

    public record Payload(
            LoanTypeId newAggregateId,
            LoanTypeId previousAggregateId,
            LoanTypeCode code,
            Title title,
            @Nullable EditReason editReason,
            GatewayType gatewayType,
            LoanApplicationStatus loanApplicationAllowed,
            SegmentType segmentType,
            Set<EconomicSector> economicSectors,
            Set<LoanTopicConfiguration<?>> loanTopicAssignments,
            Set<IncomeId> incomeIds,
            List<Attribute> attributes,
            LoanTypeGroupId groupId,
            Set<LoanArrangementId> loanArrangementIds) {

        public Payload {
            requireNonNull(newAggregateId);
            requireNonNull(previousAggregateId);
            requireNonNull(code);
            requireNonNull(title);
            requireNonNull(loanArrangementIds);
        }
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "VERSION_PREPARED";
    }
}
