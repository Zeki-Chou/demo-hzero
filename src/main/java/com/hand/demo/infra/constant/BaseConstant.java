package com.hand.demo.infra.constant;

/**
 * Constants
 */
public class BaseConstant {

    public static final Long DEFAULT_TENANT_ID = 0L;

    public static final String LANGUAGE_CN = "zh_CN";

    public static final String EMPLOYEE_ID = "47359";

    private BaseConstant() {
    }

    public static class InvApplyHeader {
        public static final String RULE_CODE = "DEMO-47359.APPLY_HEADER_NUM";
        public static final String TEMPLATE_CODE = "EXAM-47359-HEADER";
        public static final String APPLY_STATUS_CODE = "DEMO-47359.INV_APPLY_HEADER.APPLY_STATUS";
        public static final String INVOICE_COLOR_CODE = "DEMO-47359.INV_APPLY_HEADER.INV_COLOR";
        public static final String INVOICE_TYPE_CODE = "DEMO-47359.INV_APPLY_HEADER.INV_TYPE";
        public static final String APPLY_STATUS_FAILED = "F";
        public static final String INVOICE_TYPE_EINVOICE = "E";
        public static final String INVOICE_COLOR_RED = "R";
        public static final String CACHE_PREFIX = "-applyheader-" + EMPLOYEE_ID;
    }

    public static class Redis {
        public static final Long EXPIRE_DURATION = 60L;
    }

    public static class Iam {
        public static final String IAM_REALNAME = "realName";
        public static final String IAM_TENANTNAME = "tenantName";
    }


}
