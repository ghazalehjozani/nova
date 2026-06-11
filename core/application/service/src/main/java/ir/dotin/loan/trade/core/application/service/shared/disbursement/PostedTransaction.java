package ir.dotin.loan.trade.core.application.service.shared.disbursement;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;

public record PostedTransaction(String transactionNumber, String trackingId, TransactionStatus status) {}
