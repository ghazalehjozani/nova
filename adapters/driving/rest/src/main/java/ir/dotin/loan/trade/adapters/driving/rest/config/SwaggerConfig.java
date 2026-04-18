package ir.dotin.loan.trade.adapters.driving.rest.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import ir.dotin.platform.adapter.rest.swagger.BaseSwaggerConfig;
import ir.dotin.platform.adapter.rest.swagger.HeaderOperationCustomizer;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DefineLoanTypeRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DefineTradeLoanArrangementRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig extends BaseSwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final HeaderOperationCustomizer headerOperationCustomizer;

    public static final String TAG_FACILITY_CASE_OPENING = "Facility Case Opening";
    public static final String TAG_FACILITY_APPROVAL_SUBMISSION = "Facility Approval Submission";
    public static final String TAG_FACILITY_APPROVAL = "Facility Approval";
    public static final String TAG_FACILITY_REJECTION = "Facility Rejection";
    public static final String TAG_FACILITY_CONTRACT_ISSUANCE = "Facility Contract Issuance";
    public static final String TAG_LUMP_SUM_DISBURSEMENT = "Lump Sum Disbursement";
    public static final String TAG_REGULAR_DISBURSEMENT = "Regular Disbursement";
    public static final String TAG_IRREGULAR_DISBURSEMENT = "Irregular Disbursement";
    public static final String TAG_FACILITY_COLLATERAL_MANAGEMENT = "Facility Collateral Management";
    public static final String TAG_FACILITY_CLOSURE_PAID_OFF = "Facility Closure - Paid Off";
    public static final String TAG_FACILITY_CLOSURE_DEFAULTED = "Facility Closure - Defaulted";
    public static final String TAG_FACILITY_CANCELLATION = "Facility Cancellation";
    public static final String TAG_LOAN_TYPE_MANAGEMENT = "Loan Type Management";
    public static final String TAG_LOAN_ARRANGEMENT_MANAGEMENT = "Loan Arrangement Management";
    public static final String TAG_FACILITY_QUERIES = "Facility Queries";
    public static final String TAG_INSTALLMENT_SCHEDULE_QUERIES = "Installment Schedule Queries";
    public static final String TAG_LOAN_TYPE_QUERIES = "Loan Type Queries";
    public static final String TAG_LOAN_ARRANGEMENT_QUERIES = "Loan Arrangement Queries";
    public static final String TAG_FULL_LIFECYCLE = "Full Loan Facility Lifecycle";
    public static final String TAG_FACILITY_COMPENSATION = "Facility Compensation";

    private static final Map<String, Integer> TAG_ORDER = Map.ofEntries(
            Map.entry(TAG_LOAN_ARRANGEMENT_MANAGEMENT, 1),
            Map.entry(TAG_LOAN_TYPE_MANAGEMENT, 2),
            Map.entry(TAG_FACILITY_CASE_OPENING, 3),
            Map.entry(TAG_FACILITY_APPROVAL_SUBMISSION, 4),
            Map.entry(TAG_FACILITY_APPROVAL, 5),
            Map.entry(TAG_FACILITY_REJECTION, 6),
            Map.entry(TAG_FACILITY_CONTRACT_ISSUANCE, 7),
            Map.entry(TAG_LUMP_SUM_DISBURSEMENT, 8),
            Map.entry(TAG_REGULAR_DISBURSEMENT, 9),
            Map.entry(TAG_IRREGULAR_DISBURSEMENT, 10),
            Map.entry(TAG_FACILITY_COLLATERAL_MANAGEMENT, 11),
            Map.entry(TAG_FACILITY_CLOSURE_PAID_OFF, 12),
            Map.entry(TAG_FACILITY_CLOSURE_DEFAULTED, 13),
            Map.entry(TAG_FACILITY_CANCELLATION, 14),
            Map.entry(TAG_FACILITY_QUERIES, 15),
            Map.entry(TAG_INSTALLMENT_SCHEDULE_QUERIES, 16),
            Map.entry(TAG_LOAN_TYPE_QUERIES, 17),
            Map.entry(TAG_LOAN_ARRANGEMENT_QUERIES, 18));

    private OpenApiCustomizer sortTagsCustomizer() {
        return openApi -> {
            var tags = openApi.getTags();
            if (tags != null) {
                tags.sort((tag1, tag2) -> {
                    int order1 = TAG_ORDER.getOrDefault(tag1.getName(), 999);
                    int order2 = TAG_ORDER.getOrDefault(tag2.getName(), 999);
                    return Integer.compare(order1, order2);
                });
            }
        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return createOpenAPI().info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title(applicationName)
                .version(applicationVersion)
                .description("Trade Loan API")
                .contact(new Contact().name("Loan Team"));
    }

    @Override
    protected String getContextPath() {
        return contextPath;
    }

    @Override
    protected boolean isDevProfile() {
        return !"prod".equals(activeProfile);
    }

    @Bean
    public OpenApiCustomizer schemaExampleCustomizer() {
        Map<String, String> schemaExamples = Map.of(
                DefineLoanTypeRequest.class.getSimpleName(), "swagger/define-loan-type.json",
                DefineTradeLoanArrangementRequest.class.getSimpleName(), "swagger/define-loan-arrangement.json",
                OriginateLoanFacilityRequest.class.getSimpleName(), "swagger/originate-loan-facility.json");

        return openApi -> openApi.getComponents().getSchemas().forEach((name, schema) -> {
            if (schemaExamples.containsKey(name)) {
                schema.setExample(loadJson(schemaExamples.get(name)));
            }
        });
    }

    private Object loadJson(String path) {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + path);

            if (!resource.exists()) {
                throw new IllegalStateException("Swagger example file not found: " + path);
            }
            return objectMapper.readValue(resource.getInputStream(), Object.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load Swagger example: " + path, e);
        }
    }

    @Bean
    public GroupedOpenApi publicApiV1() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/{version}/**")
                .addOperationCustomizer(headerOperationCustomizer)
                .addOpenApiCustomizer(sortTagsCustomizer())
                .addOpenApiCustomizer(replaceVersionPlaceholder("v1"))
                .build();
    }

    private OpenApiCustomizer replaceVersionPlaceholder(String version) {
        return openApi -> {
            var paths = openApi.getPaths();
            var newPaths = new Paths();

            paths.forEach((path, pathItem) -> {
                String newPath = path.replace("{version}", version);

                if (pathItem.readOperations() != null) {
                    pathItem.readOperations().forEach(operation -> {
                        List<Parameter> parameters = operation.getParameters();
                        if (parameters != null) {
                            parameters.removeIf(p -> "version".equals(p.getName()));
                        }
                    });
                }
                newPaths.addPathItem(newPath, pathItem);
            });

            openApi.setPaths(newPaths);
        };
    }
}
