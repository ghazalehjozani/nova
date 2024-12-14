package ir.dotin.loan.morabehe.core.application.service.config.route;

import ir.dotin.loan.baseloan.core.application.service.config.route.BaseRoutes;

public final class LoanTypeRoutes extends BaseRoutes {

    public static final String CREATE_LOAN_TYPE_URI = "direct:createLoanType";
    public static final String COMPENSATE_CREATE_LOAN_TYPE_URI = "direct:compensateCreateLoanType";

    public static final class SagaRoutes {

        public static final String CREATE_LOAN_TYPE_SAGA = "CreateLoanTypeSaga";
        public static final String CREATE_LOAN_TYPE_SAGA_COMPENSATION = CREATE_LOAN_TYPE_SAGA + BaseRoutes.SagaRoutes.COMPENSATION;

        private SagaRoutes() {
        }

    }

    public static final class ApiEndpoints {
        public static final String BASE_PATH = BaseRoutes.ApiEndpoints.BASE_PATH + "/loan-type";

        public static final String CREATE = "/create";
        public static final String COMPENSATE_CREATE = BaseRoutes.ApiEndpoints.COMPENSATE + CREATE;

        private ApiEndpoints() {
        }

    }

    public LoanTypeRoutes() {
        super();
    }
}
