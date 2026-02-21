package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.util.List;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class PostTransactionRequest extends FcbKafkaBaseRequest {

    private final String trackingId;
    private final String facilityId;
    private final String branchCode;
    private final String documentComment;
    private final List<ArticleDto> articles;

    public PostTransactionRequest(
            String trackingId,
            String facilityId,
            String branchCode,
            String documentComment,
            List<ArticleDto> articles) {
        super("issue-general-document");
        this.trackingId = trackingId;
        this.facilityId = facilityId;
        this.branchCode = branchCode;
        this.documentComment = documentComment;
        this.articles = articles;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public String getDocumentComment() {
        return documentComment;
    }

    public List<ArticleDto> getArticles() {
        return articles;
    }
}
