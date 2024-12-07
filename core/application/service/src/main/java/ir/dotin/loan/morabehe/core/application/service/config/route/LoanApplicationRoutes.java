package ir.dotin.loan.morabehe.core.application.service.config.route;


import ir.dotin.loan.baseloan.application.service.config.route.BaseRoutes;


public final class LoanApplicationRoutes extends BaseRoutes {

    public static final String CREATE_LOAN_APPLICATION_URI = "direct:createLoanApplication";
    public static final String COMPENSATE_CREATE_LOAN_APPLICATION_URI = "direct:compensateCreateLoanApplication";

    public static final String APPROVE_LOAN_APPLICATION_URI = "direct:approveLoanApplication";
    public static final String COMPENSATE_APPROVE_LOAN_APPLICATION_URI = "direct:compensateApproveLoanApplication";

    public static final class SagaRoutes {

        public static final String CREATE_LOAN_APPLICATION_SAGA = "CreateLoanApplicationSaga";
        public static final String CREATE_LOAN_APPLICATION_SAGA_COMPENSATION = CREATE_LOAN_APPLICATION_SAGA + BaseRoutes.SagaRoutes.COMPENSATION;

        public static final String APPROVE_LOAN_APPLICATION_SAGA = "ApproveLoanApplicationSaga";
        public static final String APPROVE_LOAN_APPLICATION_SAGA_COMPENSATION = APPROVE_LOAN_APPLICATION_SAGA + BaseRoutes.SagaRoutes.COMPENSATION;

        private SagaRoutes() {
        }

    }

    public static final class ApiEndpoints {
        public static final String BASE_PATH = BaseRoutes.ApiEndpoints.BASE_PATH + "/loan-application";

        public static final String CREATE = "/create";
        public static final String COMPENSATE_CREATE = BaseRoutes.ApiEndpoints.COMPENSATE + CREATE;

        public static final String APPROVE = "/approve";
        public static final String COMPENSATE_APPROVE = BaseRoutes.ApiEndpoints.COMPENSATE + APPROVE;

        private ApiEndpoints() {
        }

    }

    private LoanApplicationRoutes() {
        super();
    }

}
