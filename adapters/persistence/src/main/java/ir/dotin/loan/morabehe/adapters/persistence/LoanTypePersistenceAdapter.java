package ir.dotin.loan.morabehe.adapters.persistence;


import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.LoanTypeCode;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanTypeDocument;
import ir.dotin.loan.morabehe.adapters.persistence.mapper.MorabeheLoanTypeDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.repository.MorabeheLoanTypeRepository;
import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class LoanTypePersistenceAdapter implements MorabeheLoanTypePersistencePort {

    private final MongoTemplate mongoTemplate;
    private final MorabeheLoanTypeRepository repository;
    private final MorabeheLoanTypeDocumentMapper mapper;

    public LoanTypePersistenceAdapter(MongoTemplate mongoTemplate,
                                      MorabeheLoanTypeRepository repository,
                                      MorabeheLoanTypeDocumentMapper mapper) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public void save(MorabeheLoanType loanType) {
        MorabeheLoanTypeDocument loanTypeDocument = mapper.mapToDocument(loanType);
        repository.save(loanTypeDocument);
    }

    @Override
    public Optional<MorabeheLoanType> findById(MorabeheLoanTypeId id) {
        return repository.findById(id.value()).map(mapper::mapToAggregate);
    }

    @Override
    public Optional<MorabeheLoanType> findByIdAndLoanRuleId(MorabeheLoanTypeId id,
                                                            MorabeheLoanRuleId loanRuleId) {
        return repository.findByIdAndLoanRuleIdAndLoanType_DisableFalse(id.value(),
                                                                        loanRuleId.value())
                .map(mapper::mapToAggregate);
    }

    @Override
    public boolean existsByCode(LoanTypeCode code) {
        return repository.existsByLoanType_Code(code.value());
    }
}
