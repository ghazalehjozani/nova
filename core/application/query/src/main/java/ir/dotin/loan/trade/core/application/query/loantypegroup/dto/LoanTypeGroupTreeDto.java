package ir.dotin.loan.trade.core.application.query.loantypegroup.dto;

import java.util.List;
import java.util.UUID;

import ir.dotin.platform.pangaea.protocol.projection.api.ProjectableResource;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

@ProjectableResource(
        views =
                @ProjectableResource.View(
                        name = "SUMMARY",
                        fields = {"id", "code", "title"}))
public record LoanTypeGroupTreeDto(
        UUID id, String code, String title, List<LoanTypeGroupTreeDto> children, List<LoanTypeRefDto> loanTypes)
        implements QueryResult {}
