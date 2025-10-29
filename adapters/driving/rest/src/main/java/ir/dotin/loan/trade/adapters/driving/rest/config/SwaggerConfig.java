package ir.dotin.loan.trade.adapters.driving.rest.config;

import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import ir.dotin.platform.adapter.rest.swagger.BaseSwaggerConfig;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig extends BaseSwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

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

    private static final Map<String, Integer> TAG_ORDER = Map.ofEntries(
            Map.entry(TAG_FACILITY_CASE_OPENING, 1),
            Map.entry(TAG_FACILITY_APPROVAL_SUBMISSION, 2),
            Map.entry(TAG_FACILITY_APPROVAL, 3),
            Map.entry(TAG_FACILITY_REJECTION, 4),
            Map.entry(TAG_FACILITY_CONTRACT_ISSUANCE, 5),
            Map.entry(TAG_LUMP_SUM_DISBURSEMENT, 6),
            Map.entry(TAG_REGULAR_DISBURSEMENT, 7),
            Map.entry(TAG_IRREGULAR_DISBURSEMENT, 8),
            Map.entry(TAG_FACILITY_COLLATERAL_MANAGEMENT, 9),
            Map.entry(TAG_FACILITY_CLOSURE_PAID_OFF, 10),
            Map.entry(TAG_FACILITY_CLOSURE_DEFAULTED, 11),
            Map.entry(TAG_FACILITY_CANCELLATION, 12),
            Map.entry(TAG_LOAN_TYPE_MANAGEMENT, 13),
            Map.entry(TAG_LOAN_ARRANGEMENT_MANAGEMENT, 14),
            Map.entry(TAG_FACILITY_QUERIES, 15),
            Map.entry(TAG_INSTALLMENT_SCHEDULE_QUERIES, 16),
            Map.entry(TAG_LOAN_TYPE_QUERIES, 17),
            Map.entry(TAG_LOAN_ARRANGEMENT_QUERIES, 18));

    @Bean
    @Order(1)
    public OpenApiCustomizer sortTagsCustomizer() {
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
    protected String getTokenUrl() {
        return contextPath.isEmpty() ? "/api/dev/auth/token" : contextPath + "/api/dev/auth/token";
    }

    @Override
    protected boolean isDevProfile() {
        return "dev".equals(activeProfile);
    }
}
