package ir.dotin.loan.morabehe.core.application.service.config.route;

import ir.dotin.loan.baseloan.core.application.service.config.route.BaseRoutes;

public final class LoanRuleRoutes extends BaseRoutes {

    public static final String CREATE_LOAN_RULE_URI = "direct:createLoanRule";
    public static final String COMPENSATE_CREATE_LOAN_RULE_URI = "direct:compensateCreateLoanRule";

    public static final String UPDATE_LOAN_RULE_URI = "direct:updateLoanRule";
    public static final String COMPENSATE_UPDATE_LOAN_RULE_URI = "direct:compensateUpdateLoanRule";

    public static final class SagaRoutes {

        public static final String CREATE_LOAN_RULE_SAGA = "CreateLoanRuleSaga";
        public static final String CREATE_LOAN_RULE_SAGA_COMPENSATION =
                CREATE_LOAN_RULE_SAGA + BaseRoutes.SagaRoutes.COMPENSATION;

        public static final String UPDATE_LOAN_RULE_SAGA = "UpdateLoanRuleSaga";
        public static final String UPDATE_LOAN_RULE_SAGA_COMPENSATION =
                UPDATE_LOAN_RULE_SAGA + BaseRoutes.SagaRoutes.COMPENSATION;

        private SagaRoutes() {}
    }

    public static final class ApiEndpoints {
        public static final String BASE_PATH = BaseRoutes.ApiEndpoints.BASE_PATH + "/loan-rule";

        public static final String CREATE = "/create";
        public static final String COMPENSATE_CREATE = BaseRoutes.ApiEndpoints.COMPENSATE + CREATE;

        public static final String UPDATE = "/update";
        public static final String COMPENSATE_UPDATE = BaseRoutes.ApiEndpoints.COMPENSATE + UPDATE;

        private ApiEndpoints() {}
    }

    public LoanRuleRoutes() {
        super();
    }
}
