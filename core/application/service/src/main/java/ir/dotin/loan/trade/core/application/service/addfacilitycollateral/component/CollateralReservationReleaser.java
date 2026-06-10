package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;

@Component
public class CollateralReservationReleaser {

    private final CollateralServicePort collateralServicePort;

    public CollateralReservationReleaser(CollateralServicePort collateralServicePort) {
        this.collateralServicePort = collateralServicePort;
    }

    public void release(ApplicationNumber applicationNumber, List<String> serials, UUID rollBackId) {
        for (String serialValue : serials) {
            CollateralSerial serial = CollateralSerial.of(serialValue).unwrap();
            collateralServicePort.unReserveCollateral(serial, applicationNumber, UUID.randomUUID(), rollBackId);
        }
    }
}
