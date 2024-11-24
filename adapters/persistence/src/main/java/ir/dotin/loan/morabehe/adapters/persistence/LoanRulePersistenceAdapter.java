package ir.dotin.loan.morabehe.adapters.persistence;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleCode;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanRuleDocument;
import ir.dotin.loan.morabehe.adapters.persistence.mapper.MorabeheLoanRuleDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.repository.MorabeheLoanRuleRepository;
import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class LoanRulePersistenceAdapter implements MorabeheLoanRulePersistencePort {

    private final MorabeheLoanRuleRepository repository;
    private final MorabeheLoanRuleDocumentMapper mapper;

    public LoanRulePersistenceAdapter(MorabeheLoanRuleRepository repository,
                                      MorabeheLoanRuleDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public void save(MorabeheLoanRule rule) {
        MorabeheLoanRuleDocument loanRuleDocument = mapper.mapToDocument(rule);
        repository.save(loanRuleDocument);
    }

    @Override
    public Optional<MorabeheLoanRule> findById(MorabeheLoanRuleId id) {
        return repository.findById(id.value()).map(mapper::mapToAggregate);
    }

    @Override
    public boolean existsByCode(LoanRuleCode code) {
        return repository.existsByLoanRule_Code(code.value());
    }

}
