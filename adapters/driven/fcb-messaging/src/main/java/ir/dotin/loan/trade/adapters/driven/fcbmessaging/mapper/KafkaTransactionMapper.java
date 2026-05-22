package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ir.dotin.platform.accounting.document.api.enumeration.Direction;
import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.Article;
import ir.dotin.platform.accounting.document.api.model.Document;
import ir.dotin.platform.accounting.document.api.model.target.AccountNumberTarget;
import ir.dotin.platform.accounting.document.api.model.target.AccountTarget;
import ir.dotin.platform.accounting.document.api.model.target.ArticleTarget;
import ir.dotin.platform.accounting.document.api.model.target.BoxTarget;
import ir.dotin.platform.accounting.document.api.model.target.DepositTarget;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.error.CoreCommonErrors;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.TransactionResultKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.DocumentItemDto;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.DocumentItemTypeDto;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ExtraInfoMetadataDto;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.PostTransactionRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.TransactionDirectionDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class KafkaTransactionMapper {

    public static Result<PostTransactionRequest> mapToIssueDocumentRequest(
            LoanTransaction loanTransaction, UUID trackingId) {

        log.debug(
                "Mapping LoanTransaction to PostTransactionRequest - facilityId: {}",
                loanTransaction.loanFacilityId().value());

        Notification notification = Notification.create();

        Notification validationResult = loanTransaction.validate();
        if (validationResult.hasErrors()) {
            log.error("LoanTransaction validation failed: {}", validationResult.getErrorMessages());
            return Result.failure(notification.merge(validationResult));
        }

        Document document = loanTransaction.document();

        String branchCode = document.branchCode().value();
        if (branchCode.isBlank()) {
            return Result.failure(notification.addError(CoreCommonErrors.GENERAL_FIELD_REQUIRED, "Branch code"));
        }

        String isoCode = extractIsoCode(loanTransaction);
        if (isoCode == null || isoCode.isBlank()) {
            return Result.failure(
                    notification.addError(CoreCommonErrors.GENERAL_FIELD_REQUIRED, "ISO code (currency)"));
        }

        String comment = document.description();
        if (comment.isBlank()) {
            return Result.failure(
                    notification.addError(CoreCommonErrors.GENERAL_FIELD_REQUIRED, "Document description"));
        }

        Result<List<DocumentItemDto>> itemsResult = mapArticles(document.articles());
        if (itemsResult.isFailure()) {
            return Result.failure(
                    notification.merge(itemsResult.err().orElseThrow().notification()));
        }

        // TODO: Fix this
        ExtraInfoMetadataDto documentMetadata = pickDocumentLevelMetadata(document);

        PostTransactionRequest request = PostTransactionRequest.builder()
                .transactionId(String.valueOf(trackingId))
                .comment(comment)
                .isoCode(isoCode)
                .branchCode(branchCode)
                .skipTransferMoneyBillNumber(false)
                .items(itemsResult.unwrap())
                .documentMetadata(documentMetadata)
                .build();

        log.info(
                "Successfully mapped LoanTransaction to PostTransactionRequest - items count: {}",
                request.getItems().size());

        return Result.success(request);
    }

    public static Result<List<DocumentItemDto>> mapArticles(List<Article> articles) {
        Notification notification = Notification.create();

        if (articles == null || articles.isEmpty()) {
            return Result.failure(notification.addError(CoreCommonErrors.GENERAL_FIELD_REQUIRED, "Articles list"));
        }

        List<DocumentItemDto> items = new ArrayList<>(articles.size());
        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            Result<DocumentItemDto> itemResult = mapArticle(article, i);
            if (itemResult.isFailure()) {
                notification.merge(itemResult.err().orElseThrow().notification());
                continue;
            }
            items.add(itemResult.unwrap());
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }
        return Result.success(items);
    }

    private static Result<DocumentItemDto> mapArticle(Article article, int index) {
        Notification notification = Notification.create();

        if (article == null) {
            return Result.failure(
                    notification.addError(CoreCommonErrors.GENERAL_FIELD_REQUIRED, "Article[" + index + "]"));
        }

        TargetDescriptor descriptor = describeTarget(article.target());
        if (descriptor.type() == null) {
            return Result.failure(notification.addError(
                    CoreCommonErrors.GENERAL_FIELD_REQUIRED, "Unknown article target type at index " + index));
        }

        TransactionDirectionDto direction = mapDirection(article.direction());
        ExtraInfoMetadataDto metadata = ArticleMetadataMapper.toDto(article.articleMetadata());

        String title = buildTitle(descriptor, direction);

        DocumentItemDto item = DocumentItemDto.builder()
                .type(descriptor.type())
                .identifier(descriptor.identifier())
                .direction(direction)
                .amount(article.amount().value())
                .title(title)
                .metadata(metadata)
                .build();

        log.debug(
                "Mapped article {} -> {} {} {} {}",
                index,
                descriptor.type(),
                descriptor.identifier(),
                direction,
                item.getAmount());

        return Result.success(item);
    }

    private static TransactionDirectionDto mapDirection(Direction direction) {
        return direction == Direction.DEBIT ? TransactionDirectionDto.DEBTOR : TransactionDirectionDto.CREDITOR;
    }

    private static TargetDescriptor describeTarget(ArticleTarget target) {
        return switch (target) {
            case AccountTarget(var accountId, RelationType<?> ignored) ->
                new TargetDescriptor(DocumentItemTypeDto.ACCOUNT, accountId.value());
            case DepositTarget(var depositNumber) ->
                new TargetDescriptor(DocumentItemTypeDto.DEPOSIT, depositNumber.value());
            case BoxTarget() -> new TargetDescriptor(DocumentItemTypeDto.BOX, "");
            case AccountNumberTarget(var accountNumber) ->
                new TargetDescriptor(DocumentItemTypeDto.ACCOUNT, accountNumber.accountNumber());
        };
    }

    private static String buildTitle(TargetDescriptor descriptor, TransactionDirectionDto direction) {
        String dirText = direction == TransactionDirectionDto.DEBTOR ? "بدهکاری" : "بستانکاری";
        String typeText =
                switch (descriptor.type()) {
                    case ACCOUNT -> "حساب";
                    case DEPOSIT -> "سپرده";
                    case BOX -> "صندوق";
                };
        String id = descriptor.identifier() == null || descriptor.identifier().isBlank()
                ? typeText
                : descriptor.identifier();
        return "بند سند " + dirText + " " + typeText + " - " + id;
    }

    private static ExtraInfoMetadataDto pickDocumentLevelMetadata(Document document) {
        return document.articles().stream()
                .findFirst()
                .map(Article::articleMetadata)
                .map(ArticleMetadataMapper::toDto)
                .orElse(null);
    }

    private static String extractIsoCode(LoanTransaction loanTransaction) {
        if (loanTransaction.document().isoCode().isPresent()) {
            return loanTransaction.document().isoCode().get().value();
        }
        try {
            CurrencyType currency = loanTransaction.getTransactionCurrency();
            return String.valueOf(currency.getNumericCode());
        } catch (Exception e) {
            log.error("Failed to extract ISO code from transaction", e);
            return null;
        }
    }

    public static Result<TrackedTransactionNumber> mapToTrackedTransactionNumber(
            TransactionResultKafkaResponse response, UUID trackingId, Clock clock) {

        if (response.getTransactionCode() == null
                || response.getTransactionCode().isBlank()) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "postTransaction");
        }
        Result<TransactionNumber> txnResult = TransactionNumber.of(response.getTransactionCode());
        if (txnResult.isFailure()) {
            return Result.failure(txnResult.err().orElseThrow());
        }
        return Result.success(TrackedTransactionNumber.create(
                txnResult.unwrap().value(), trackingId.toString(), TransactionStatus.POSTED, clock));
    }

    private record TargetDescriptor(DocumentItemTypeDto type, String identifier) {}
}
