package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.vo.LoanTypeGroupCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity.LoanTypeGroupEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.mapper.LoanTypeGroupPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.mapper.LoanTypeGroupPersistenceMapperImpl;

import static org.assertj.core.api.Assertions.assertThat;

class LoanTypeGroupPersistenceMapperTest {

    private final LoanTypeGroupPersistenceMapper mapper = new LoanTypeGroupPersistenceMapperImpl();

    @Test
    void roundTripsTitleParentAndVersion() {
        LoanTypeGroupId id = LoanTypeGroupId.generate();
        LoanTypeGroupId parent = LoanTypeGroupId.generate();
        LoanTypeGroup domain =
                LoanTypeGroup.reconstitute(id, new LoanTypeGroupCode("CD-1"), new Title("کالا"), parent, 3L);

        LoanTypeGroupEntity entity = mapper.map(domain);

        assertThat(entity.getId()).isEqualTo(id.value());
        assertThat(entity.getCode()).isEqualTo("CD-1");
        assertThat(entity.getTitle()).isEqualTo("کالا");
        assertThat(entity.getParentGroupId()).isEqualTo(parent.value());
        assertThat(entity.getVersion()).isEqualTo(3);

        LoanTypeGroup back = mapper.map(entity);

        assertThat(back.getId()).isEqualTo(id);
        assertThat(back.getCode().value()).isEqualTo("CD-1");
        assertThat(back.getTitle().value()).isEqualTo("کالا");
        assertThat(back.getParentGroupId()).isEqualTo(parent);
        assertThat(back.getVersion()).isEqualTo(3L);
    }

    @Test
    void roundTripsNullParentAndNullVersion() {
        LoanTypeGroupId id = LoanTypeGroupId.generate();
        LoanTypeGroup domain =
                LoanTypeGroup.reconstitute(id, new LoanTypeGroupCode("CD-2"), new Title("ریشه"), null, null);

        LoanTypeGroupEntity entity = mapper.map(domain);

        assertThat(entity.getParentGroupId()).isNull();
        assertThat(entity.getVersion()).isNull();

        LoanTypeGroup back = mapper.map(entity);

        assertThat(back.getParentGroupId()).isNull();
        assertThat(back.getVersion()).isNull();
    }
}
