package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper;

import org.mapstruct.Mapper;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;

@Mapper(config = BaseMapperConfig.class)
public interface AddFacilityCollateralCommandMapper {

    CollateralSerial toCollateralSerial(AddFacilityCollateralCommand.CollateralSerialDto dto);
}
