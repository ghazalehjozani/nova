package ir.dotin.loan.trade.adapters.driving.rest.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import ir.dotin.platform.pangaea.protocol.rest.config.RestAdapterProperties;
import ir.dotin.platform.pangaea.protocol.rest.partial.PartialResponseOperationCustomizer;
import ir.dotin.platform.pangaea.protocol.rest.swagger.BaseSwaggerConfig;
import ir.dotin.platform.pangaea.protocol.rest.swagger.HeaderOperationCustomizer;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DefineLoanTypeRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DefineTradeLoanArrangementRequest;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.tags.Tag;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SwaggerConfig extends BaseSwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    private final RestAdapterProperties restAdapterProperties;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final HeaderOperationCustomizer headerOperationCustomizer;
    private final PartialResponseOperationCustomizer partialResponseOperationCustomizer;

    public SwaggerConfig(
            RestAdapterProperties restAdapterProperties,
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader,
            HeaderOperationCustomizer headerOperationCustomizer,
            PartialResponseOperationCustomizer partialResponseOperationCustomizer) {
        super(restAdapterProperties.getSwagger());
        this.restAdapterProperties = restAdapterProperties;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.headerOperationCustomizer = headerOperationCustomizer;
        this.partialResponseOperationCustomizer = partialResponseOperationCustomizer;
    }

    // Two tags per aggregate: a Commands (write) and a Queries (read) tag. Controllers reference the granular
    // constants below; the constants collapse onto these eight canonical tag names so command and query operations of
    // the same aggregate render as sibling "<Aggregate> · Commands" / "<Aggregate> · Queries" sections, nested under
    // the aggregate via the x-tagGroups extension (ReDoc) and ordered Commands-before-Queries (stock Swagger UI).
    public static final String TAG_LOAN_TYPES_COMMANDS = "Loan Types · Commands";
    public static final String TAG_LOAN_TYPES_QUERIES = "Loan Types · Queries";
    public static final String TAG_LOAN_TYPE_GROUP_COMMANDS = "Loan Type Groups · Commands";
    public static final String TAG_LOAN_TYPE_GROUP_QUERIES = "Loan Type Groups · Queries";
    public static final String TAG_LOAN_ARRANGEMENTS_COMMANDS = "Loan Arrangements · Commands";
    public static final String TAG_LOAN_ARRANGEMENTS_QUERIES = "Loan Arrangements · Queries";
    public static final String TAG_LOAN_FACILITIES_COMMANDS = "Loan Facilities · Commands";
    public static final String TAG_LOAN_FACILITIES_QUERIES = "Loan Facilities · Queries";
    public static final String TAG_INSTALLMENT_SCHEDULES_QUERIES = "Installment Schedules · Queries";
    public static final String TAG_FORMULAS_COMMANDS = "Formulas · Commands";
    public static final String TAG_FORMULAS_QUERIES = "Formulas · Queries";
    public static final String TAG_RECONCILIATION_OPS = "Reconciliation · Ops";

    private static final String GROUP_LOAN_TYPES = "Loan Types";
    private static final String GROUP_LOAN_TYPE_GROUPS = "Loan Type Groups";
    private static final String GROUP_LOAN_ARRANGEMENTS = "Loan Arrangements";
    private static final String GROUP_LOAN_FACILITIES = "Loan Facilities";
    private static final String GROUP_INSTALLMENT_SCHEDULES = "Installment Schedules";
    private static final String GROUP_FORMULAS = "Formulas";
    private static final String GROUP_RECONCILIATION = "Reconciliation";

    // ---- Aliases referenced by controllers (value-only mapping onto the eight canonical tags) ----
    public static final String TAG_LOAN_TYPE_MANAGEMENT = TAG_LOAN_TYPES_COMMANDS;
    public static final String TAG_LOAN_TYPE_QUERIES = TAG_LOAN_TYPES_QUERIES;
    public static final String TAG_LOAN_ARRANGEMENT_MANAGEMENT = TAG_LOAN_ARRANGEMENTS_COMMANDS;
    public static final String TAG_LOAN_ARRANGEMENT_QUERIES = TAG_LOAN_ARRANGEMENTS_QUERIES;
    public static final String TAG_FACILITY_CASE_OPENING = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_APPROVAL_SUBMISSION = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_APPROVAL = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_REJECTION = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_CONTRACT_ISSUANCE = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_LUMP_SUM_DISBURSEMENT = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_REGULAR_DISBURSEMENT = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_IRREGULAR_DISBURSEMENT = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_COLLATERAL_MANAGEMENT = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_GUARANTOR_MANAGEMENT = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_CLOSURE_PAID_OFF = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_CLOSURE_DEFAULTED = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_CANCELLATION = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_COMPENSATION = TAG_LOAN_FACILITIES_COMMANDS;
    public static final String TAG_FACILITY_QUERIES = TAG_LOAN_FACILITIES_QUERIES;
    public static final String TAG_INSTALLMENT_SCHEDULE_QUERIES = TAG_INSTALLMENT_SCHEDULES_QUERIES;

    /** Origination examples carrying a caller-supplied instalment table (display name -> classpath JSON). */
    private static final Map<String, String> UNEQUAL_INSTALLMENT_ORIGINATE_EXAMPLES = Map.of(
            "Primary applicant only", "swagger/originate-01-primary-only.json",
            "With guarantor", "swagger/originate-02-with-guarantor.json",
            "Multiple collaterals", "swagger/originate-03-multi-collateral.json",
            "Short tenor (3 months)", "swagger/originate-04-short-tenor.json",
            "Long tenor (24 months)", "swagger/originate-05-long-tenor.json",
            "Multi-tranche disbursement", "swagger/originate-06-multi-tranche.json");

    /** Origination examples whose schedule the system generates — no instalment table on the wire. */
    private static final Map<String, String> EQUAL_INSTALLMENT_ORIGINATE_EXAMPLES =
            Map.of("Equal installments", "swagger/originate-07-equal-installments.json");

    @Bean
    public OpenAPI customOpenAPI() {
        return createOpenAPI().info(apiInfo());
    }

    // springdoc 3.x (Boot 4 native API versioning) leaves {version} literal on the ungrouped /v3/api-docs that the
    // Swagger UI loads; the v1 group rewrites it but the UI does not target the group. Apply the rewrite globally too.
    @Bean
    public OpenApiCustomizer defaultDocVersionPathCustomizer() {
        return pathProcessingCustomizer("1");
    }

    private Info apiInfo() {
        RestAdapterProperties.Swagger swagger = restAdapterProperties.getSwagger();
        Info info = new Info()
                .title(applicationName)
                .version(applicationVersion)
                .description(
                        StringUtils.hasText(swagger.getDescription()) ? swagger.getDescription() : "Trade Loan API");

        RestAdapterProperties.Swagger.Contact contact = swagger.getContact();
        if (contact != null
                && (StringUtils.hasText(contact.getName())
                        || StringUtils.hasText(contact.getEmail())
                        || StringUtils.hasText(contact.getUrl()))) {
            info.contact(
                    new Contact().name(contact.getName()).url(contact.getUrl()).email(contact.getEmail()));
        } else {
            info.contact(new Contact().name("Loan Team"));
        }

        RestAdapterProperties.Swagger.License license = swagger.getLicense();
        if (license != null && StringUtils.hasText(license.getName())) {
            info.license(new License().name(license.getName()).url(license.getUrl()));
        }
        if (StringUtils.hasText(swagger.getTermsOfService())) {
            info.termsOfService(swagger.getTermsOfService());
        }
        return info;
    }

    @Bean
    public GroupedOpenApi publicApiV1() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/v{version}/**")
                // MCP JSON-RPC endpoints are functional servlets, not MVC routes; springdoc cannot model them. They
                // are documented separately by the pangaea-ai-mcp-server "mcp" GroupedOpenApi. Exclude their paths
                // (and the /v1/mcp/* sub-paths the stateless transport registers) from the business v1 group.
                .pathsToExclude("/v1/mcp", "/v1/mcp/**", "/v1/ops/mcp", "/v1/ops/mcp/**")
                .addOperationCustomizer(headerOperationCustomizer)
                // A GroupedOpenApi with an explicit operation-customizer list does NOT inherit other global
                // OperationCustomizer beans, so the platform partial-response customizer must be added here too —
                // otherwise fields/view/expand show only on the default /v3/api-docs, never on this v1 group.
                .addOperationCustomizer(partialResponseOperationCustomizer)
                .addOpenApiCustomizer(tagsCustomizer())
                .addOpenApiCustomizer(externalDocsCustomizer())
                .addOpenApiCustomizer(pathProcessingCustomizer("1"))
                .addOpenApiCustomizer(schemaExampleCustomizer())
                .addOpenApiCustomizer(originateExamplesCustomizer())
                .build();
    }

    /** Defines the eight aggregate Command/Query tags (ordered) and nests them under aggregate groups (x-tagGroups). */
    private OpenApiCustomizer tagsCustomizer() {
        return openApi -> {
            openApi.setTags(List.of(
                    tag(TAG_LOAN_TYPES_COMMANDS, "Write operations (commands) for loan types."),
                    tag(TAG_LOAN_TYPES_QUERIES, "Read operations (queries) for loan types."),
                    tag(TAG_LOAN_TYPE_GROUP_COMMANDS, "Write operations (commands) for loan type groups."),
                    tag(TAG_LOAN_TYPE_GROUP_QUERIES, "Read operations (queries) for loan type groups."),
                    tag(TAG_LOAN_ARRANGEMENTS_COMMANDS, "Write operations (commands) for loan arrangements."),
                    tag(TAG_LOAN_ARRANGEMENTS_QUERIES, "Read operations (queries) for loan arrangements."),
                    tag(TAG_LOAN_FACILITIES_COMMANDS, "Write operations (commands) for loan facilities."),
                    tag(TAG_LOAN_FACILITIES_QUERIES, "Read operations (queries) for loan facilities."),
                    tag(TAG_INSTALLMENT_SCHEDULES_QUERIES, "Read operations (queries) for installment schedules."),
                    tag(TAG_FORMULAS_COMMANDS, "Write operations (commands) for formulas."),
                    tag(TAG_FORMULAS_QUERIES, "Read operations (queries) for formulas."),
                    tag(TAG_RECONCILIATION_OPS, "Reconciliation ops: discrepancy lookup, convergence, remediation.")));

            openApi.addExtension(
                    "x-tagGroups",
                    List.of(
                            tagGroup(GROUP_LOAN_TYPES, List.of(TAG_LOAN_TYPES_COMMANDS, TAG_LOAN_TYPES_QUERIES)),
                            tagGroup(
                                    GROUP_LOAN_TYPE_GROUPS,
                                    List.of(TAG_LOAN_TYPE_GROUP_COMMANDS, TAG_LOAN_TYPE_GROUP_QUERIES)),
                            tagGroup(
                                    GROUP_LOAN_ARRANGEMENTS,
                                    List.of(TAG_LOAN_ARRANGEMENTS_COMMANDS, TAG_LOAN_ARRANGEMENTS_QUERIES)),
                            tagGroup(
                                    GROUP_LOAN_FACILITIES,
                                    List.of(TAG_LOAN_FACILITIES_COMMANDS, TAG_LOAN_FACILITIES_QUERIES)),
                            tagGroup(GROUP_INSTALLMENT_SCHEDULES, List.of(TAG_INSTALLMENT_SCHEDULES_QUERIES)),
                            tagGroup(GROUP_FORMULAS, List.of(TAG_FORMULAS_COMMANDS, TAG_FORMULAS_QUERIES)),
                            tagGroup(GROUP_RECONCILIATION, List.of(TAG_RECONCILIATION_OPS))));
        };
    }

    /** Sets the externalDocs (static AsyncAPI document link) on the v1 group spec, sourced from config. */
    private OpenApiCustomizer externalDocsCustomizer() {
        return openApi -> {
            RestAdapterProperties.Swagger swagger = restAdapterProperties.getSwagger();
            if (StringUtils.hasText(swagger.getExternalDocsUrl())) {
                openApi.externalDocs(new ExternalDocumentation()
                        .url(swagger.getExternalDocsUrl())
                        .description(swagger.getExternalDocsDescription()));
            }
        };
    }

    private Tag tag(String name, String description) {
        return new Tag().name(name).description(description);
    }

    private Map<String, Object> tagGroup(String name, List<String> tags) {
        Map<String, Object> group = new LinkedHashMap<>();
        group.put("name", name);
        group.put("tags", tags);
        return group;
    }

    /**
     * Replaces the {@code {version}} path placeholder, drops the synthetic {@code version} parameter, and reorders the
     * paths so each aggregate is contiguous and Loan Facilities commands follow the business journey (originate →
     * submit → approve → collaterals → issue-contract → disburse → close → lifecycle/compensation).
     */
    private OpenApiCustomizer pathProcessingCustomizer(String version) {
        return openApi -> {
            Paths paths = openApi.getPaths();
            if (paths == null) {
                return;
            }

            Map<String, PathItem> replaced = new LinkedHashMap<>();
            paths.forEach((path, pathItem) -> {
                String newPath = path.replace("{version}", version);
                if (pathItem.readOperations() != null) {
                    pathItem.readOperations().forEach(operation -> {
                        List<Parameter> parameters = operation.getParameters();
                        if (parameters != null) {
                            parameters.removeIf(parameter -> "version".equals(parameter.getName()));
                        }
                    });
                }
                replaced.put(newPath, pathItem);
            });

            List<String> ordered = new ArrayList<>(replaced.keySet());
            ordered.sort(Comparator.comparingInt(this::aggregateRank)
                    .thenComparingInt(this::facilityJourneyRank)
                    .thenComparing(Comparator.naturalOrder()));

            Paths newPaths = new Paths();
            for (String path : ordered) {
                newPaths.addPathItem(path, replaced.get(path));
            }
            openApi.setPaths(newPaths);
        };
    }

    private int aggregateRank(String path) {
        if (path.contains("/loan-type-groups")) {
            return 1;
        }
        if (path.contains("/loan-types")) {
            return 0;
        }
        if (path.contains("/loan-arrangements")) {
            return 2;
        }
        if (path.contains("/loan-facilities")) {
            return 3;
        }
        if (path.contains("/installment-schedules")) {
            return 4;
        }
        if (path.contains("/formulas")) {
            return 5;
        }
        return 9;
    }

    private int facilityJourneyRank(String path) {
        if (!path.contains("/loan-facilities") || path.endsWith("/loan-facilities")) {
            return 0; // originate (POST) / list (GET)
        }
        if (path.contains("/submit")) {
            return 1;
        }
        if (path.contains("/reject")) {
            return 2;
        }
        if (path.contains("/approve")) {
            return 2;
        }
        if (path.contains("/collateral")) {
            return 3;
        }
        if (path.contains("/issue-contract")) {
            return 4;
        }
        if (path.contains("/disburse")) {
            return 5;
        }
        if (path.contains("/close")) {
            return 6;
        }
        if (path.contains("/compensate")) {
            return 8;
        }
        return 9;
    }

    /** Single schema-level example for the two "define" requests (kept from the previous configuration). */
    private OpenApiCustomizer schemaExampleCustomizer() {
        Map<String, String> schemaExamples = Map.of(
                DefineLoanTypeRequest.class.getSimpleName(), "swagger/define-loan-type.json",
                DefineTradeLoanArrangementRequest.class.getSimpleName(), "swagger/define-loan-arrangement.json");

        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            openApi.getComponents().getSchemas().forEach((name, schema) -> {
                if (schemaExamples.containsKey(name)) {
                    schema.setExample(loadJson(schemaExamples.get(name)));
                }
            });
        };
    }

    /** Multiple named request-body examples (from nova-loadlab) on the facility-origination operation. */
    private static Map<String, String> examplesFor(String path) {
        if (path.endsWith("/loan-facilities/equal-installments")) {
            return EQUAL_INSTALLMENT_ORIGINATE_EXAMPLES;
        }
        if (path.endsWith("/loan-facilities/unequal-installments")) {
            return UNEQUAL_INSTALLMENT_ORIGINATE_EXAMPLES;
        }
        return Map.of();
    }

    private OpenApiCustomizer originateExamplesCustomizer() {
        return openApi -> {
            Paths paths = openApi.getPaths();
            if (paths == null) {
                return;
            }
            paths.forEach((path, pathItem) -> {
                Map<String, String> examples = examplesFor(path);
                if (pathItem.getPost() == null || examples.isEmpty()) {
                    return;
                }
                var requestBody = pathItem.getPost().getRequestBody();
                if (requestBody == null || requestBody.getContent() == null) {
                    return;
                }
                var mediaType = requestBody.getContent().get("application/json");
                if (mediaType == null) {
                    return;
                }
                examples.forEach((name, file) ->
                        mediaType.addExamples(name, new Example().summary(name).value(loadJson(file))));
            });
        };
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
}
