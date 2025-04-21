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

public record MorabeheLoanTypeCreated(UUID eventId, MorabeheLoanTypeId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanTypeEvent<MorabeheLoanTypeCreated, MorabeheLoanTypeCreated.Payload> {

    public MorabeheLoanTypeCreated {
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
            Set<MorabeheLoanArrangementId> loanArrangementIds,
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
