package ir.dotin.loan.trade.core.domain.shared.service;

import java.util.Objects;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionCause;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.NetworkInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TerminalInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TransactionInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TransactionMetadata;
import ir.dotin.loan.trade.core.domain.disbursement.i18n.TradeDisbursementLocalizedMessageCodes;

@DomainService
public class TransactionMetadataResolverService {

    public Result<TransactionMetadata> resolveTransactionMetadata(
            TransactionMetadata baseMetadata,
            TransactionType defaultTransactionType,
            TransactionCause defaultTransactionCause) {

        Objects.requireNonNull(baseMetadata, "baseMetadataOpt cannot be null");
        Objects.requireNonNull(defaultTransactionType, "defaultTransactionType cannot be null");
        Objects.requireNonNull(defaultTransactionCause, "defaultTransactionCause cannot be null");

        Notification notification = Notification.create();

        Result<TransactionInfo> defaultTrxInfoResult =
                TransactionInfo.of(defaultTransactionType, defaultTransactionCause);
        Result<TerminalInfo> defaultTerminalInfoResult = TerminalInfo.of("DEFAULT_TERMINAL");
        Result<NetworkInfo> defaultNetworkInfoResult = NetworkInfo.of("INTERNAL");

        boolean hasFailure = false;
        if (defaultTrxInfoResult.isFailure()) {
            notification.merge(defaultTrxInfoResult.notification());
            hasFailure = true;
        }
        if (defaultTerminalInfoResult.isFailure()) {
            notification.merge(defaultTerminalInfoResult.notification());
            hasFailure = true;
        }
        if (defaultNetworkInfoResult.isFailure()) {
            notification.merge(defaultNetworkInfoResult.notification());
            hasFailure = true;
        }

        if (hasFailure) {
            notification = notification.addError(
                    TradeDisbursementLocalizedMessageCodes.TRANSACTION_METADATA_DEFAULT_CREATION_FAILED);
            return Result.failure(notification);
        }

        Result<TransactionMetadata> defaultMetadataResult = TransactionMetadata.of(
                defaultTrxInfoResult.value(),
                defaultTerminalInfoResult.value(),
                defaultNetworkInfoResult.value(),
                null,
                null,
                null);

        if (defaultMetadataResult.isFailure()) {
            notification = notification
                    .merge(defaultMetadataResult.notification())
                    .addError(TradeDisbursementLocalizedMessageCodes.TRANSACTION_METADATA_DEFAULT_CREATION_FAILED);
            return Result.failure(notification);
        }
        return Result.success(defaultMetadataResult.value());
    }

    public Result<TransactionMetadata> resolveForDisbursement(TransactionMetadata baseMetadata) {
        return resolveTransactionMetadata(baseMetadata, TransactionType.CODE_10004, TransactionCause.LRPA);
    }
}
