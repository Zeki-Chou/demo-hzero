package com.hand.demo.infra.constant;

public class MessageConstant {

    public static final String LANGUAGE = "zh_CN";

    // for sending through feishu
    public static final String SERVER_CODE = "FEIYU";
    public static final String MESSAGE_TEMPLATE_CODE = "DEMO-47359";

    // from request definition
    public static final String RECEIVER_EMAIL_PARAM = "email";
    public static final String SENDER_ID_PARAM = "empId";

    // message template arguments
    public static final String MESSAGE_EMAIL_ARG = "empEmail";
    public static final String MESSAGE_ID_ARG = "empId";
    public static final String MESSAGE_NAME_ARG = "empName";
}
