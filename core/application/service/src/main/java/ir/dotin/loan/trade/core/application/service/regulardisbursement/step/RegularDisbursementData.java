package ir.dotin.loan.trade.core.application.service.regulardisbursement.step;

import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RegularDisbursementCommand;

public record RegularDisbursementData(RegularDisbursementCommand command, Unit prepared) {}
