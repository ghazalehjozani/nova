package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.commandhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.i18n.AddFacilityCollateralErrorCodes;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddFacilityCollateralCommandHandler implements CommandHandler<AddFacilityCollateralCommand> {

    private static final Logger log = LoggerFactory.getLogger(AddFacilityCollateralCommandHandler.class);

    private final AddFacilityCollateralCommandMapper mapper;
    private final TradeLoanFacilityRepository repository;
    private final TradeLoanFacilityService domainService;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(AddFacilityCollateralCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> Notification.ofError(
                                AddFacilityCollateralErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId()))
                .peekValue(facility -> {
                    CollateralSerial collateralSerial = mapper.toCollateralSerial(command.collateralSerialDto());
                    domainService.addCollateral(facility, collateralSerial);
                    repository.save(facility);
                    log.debug("Collateral added to facility: {}", command.loanFacilityId());
                })
                .mapNonNull(TradeLoanFacility::domainEvents);
    }
}
