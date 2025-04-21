package ir.dotin.loan.morabehe.core.domain.loantype.event;

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
import ir.dotin.loan.morabehe.core.domain.loanarrangement.vo.MorabeheLoanArrangementId;
import ir.dotin.loan.morabehe.core.domain.loantype.vo.MorabeheLoanTypeId;

public record NewMorabeheLoanTypeVersionPrepared(
        UUID eventId, MorabeheLoanTypeId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanTypeEvent<
                NewMorabeheLoanTypeVersionPrepared, NewMorabeheLoanTypeVersionPrepared.Payload> {

    public NewMorabeheLoanTypeVersionPrepared {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(aggregateId);
        Objects.requireNonNull(payload);
        Objects.requireNonNull(createdAt);
        if (!aggregateId.equals(payload.newAggregateId()))
            throw new IllegalArgumentException("aggregateId must match payload.newAggregateId");
    }

    public record Payload(
            MorabeheLoanTypeId newAggregateId,
            MorabeheLoanTypeId previousAggregateId,
            LoanTypeCode code,
            Title title,
            EditReason editReason,
            GatewayType gatewayType,
            LoanApplicationStatus loanApplicationAllowed,
            SegmentType segmentType,
            Set<EconomicSector> economicSectors,
            Set<LoanTopicConfiguration<?>> loanTopicAssignments,
            Set<IncomeId> incomeIds,
            List<Attribute> attributes,
            LoanTypeGroupId groupId,
            Set<MorabeheLoanArrangementId> loanArrangementIds) {

        public Payload {
            Objects.requireNonNull(newAggregateId);
            Objects.requireNonNull(previousAggregateId);
            Objects.requireNonNull(code);
            Objects.requireNonNull(title);
            Objects.requireNonNull(loanArrangementIds);
        }
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "VERSION_PREPARED";
    }
}
