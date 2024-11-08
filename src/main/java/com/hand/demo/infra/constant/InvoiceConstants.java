package com.hand.demo.infra.constant;

public class InvoiceConstants {

    public static final String CODE_RULE = "DEMO-47357.INV_HEADER.INV_NUM";
    public static final String LINE = "Line No ";

    public static class LovCode {
        public static final String APPLY_STATUS = "DEMO-47357.INV_HEADER.APPLY_STATUS";// Code used to retrieve valid task types from LovAdapter
        public static final String INVOICE_COLOR = "DEMO-47357.INV_HEADER.INV_COLOR";
        public static final String INVOICE_TYPE = "DEMO-47357.INV_HEADER.INV_TYPE";
    }

    public static final String CACHE_KEY = "header_47357:" + "InvoiceID_";
    
    private InvoiceConstants() {}
}
