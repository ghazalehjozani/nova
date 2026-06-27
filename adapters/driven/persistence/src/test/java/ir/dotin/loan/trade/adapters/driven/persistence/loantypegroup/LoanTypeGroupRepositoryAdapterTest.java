package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity.LoanTypeGroupEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.repository.LoanTypeGroupJpaRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class LoanTypeGroupRepositoryAdapterTest {

    private static final UUID ROOT = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CHILD = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private LoanTypeGroupJpaRepository jpaRepository;

    @InjectMocks
    private LoanTypeGroupRepositoryAdapter adapter;

    @Test
    void findAncestorChainWalksParentsToRoot() {
        LoanTypeGroupEntity child = entity(CHILD, ROOT);
        LoanTypeGroupEntity root = entity(ROOT, null);
        when(jpaRepository.findById(CHILD)).thenReturn(Optional.of(child));
        when(jpaRepository.findById(ROOT)).thenReturn(Optional.of(root));

        List<LoanTypeGroupId> chain = adapter.findAncestorChain(new LoanTypeGroupId(CHILD));

        assertThat(chain).containsExactly(new LoanTypeGroupId(ROOT));
    }

    @Test
    void findAncestorChainStopsOnCycle() {
        LoanTypeGroupEntity child = entity(CHILD, ROOT);
        LoanTypeGroupEntity root = entity(ROOT, CHILD);
        when(jpaRepository.findById(CHILD)).thenReturn(Optional.of(child));
        when(jpaRepository.findById(ROOT)).thenReturn(Optional.of(root));

        List<LoanTypeGroupId> chain = adapter.findAncestorChain(new LoanTypeGroupId(CHILD));

        assertThat(chain).containsExactly(new LoanTypeGroupId(ROOT));
    }

    @Test
    void findAncestorChainOfRootIsEmpty() {
        LoanTypeGroupEntity root = entity(ROOT, null);
        when(jpaRepository.findById(ROOT)).thenReturn(Optional.of(root));

        List<LoanTypeGroupId> chain = adapter.findAncestorChain(new LoanTypeGroupId(ROOT));

        assertThat(chain).isEmpty();
    }

    private static LoanTypeGroupEntity entity(UUID id, UUID parentGroupId) {
        LoanTypeGroupEntity entity = new LoanTypeGroupEntity();
        entity.setId(id);
        entity.setParentGroupId(parentGroupId);
        return entity;
    }
}
