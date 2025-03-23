package ir.dotin.loan.morabehe.adapters.driving.graphql.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.loan.morabehe.core.application.service.config.route.LoanApplicationRoutes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "GraphQL", description = "Loan Application GraphQL Query Endpoint")
@RequestMapping(LoanApplicationRoutes.ApiEndpoints.BASE_GRAPHQL_PATH)
public class MorabeheLoanApplicationQueryResolver {

    //    private final MorabeheLoanApplicationQueryService queryService; // Application service interface
    //
    //    public MorabeheLoanApplicationQueryResolver(MorabeheLoanApplicationQueryService queryService) {
    //        this.queryService = queryService;
    //    }

    @QueryMapping
    @Operation(summary = "Get Loan Application By Id")
    public String morabeheLoanApplicationById(@Argument String id) {
        return "OK";
    }

    @QueryMapping
    @Operation(summary = "Get Loan Applications By Id")
    public List<String> morabeheLoanApplications() {
        return List.of("OK");
    }
}
