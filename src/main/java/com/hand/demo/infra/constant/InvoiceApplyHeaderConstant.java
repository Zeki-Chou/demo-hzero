package com.hand.demo.infra.constant;

import org.hzero.core.message.Message;
import org.hzero.core.message.MessageAccessor;

public class InvoiceApplyHeaderConstant {
    public static class LovCode {
        public static final String APPLY_STATUS = "DEMO-47361.INV_HEADER.APPLY_STATUS";
        public static final String INVOICE_COLOR = "DEMO-47361.INV_HEADER.INVOICE_COLOR";
        public static final String INVOICE_TYPE = "DEMO-47361.INV_HEADER.INVOICE_TYPE";
    }

    public static final String ERROR_SAVE = "demo-47361.invoice_apply_header.save_error";
    public static final String ERROR_NOT_FOUND = "demo-47361.invoice_apply_header.not_found_error";
    public static final String ERROR_GENERAL_MESSAGE = "demo-47361_invoice_general_error";

    public InvoiceApplyHeaderConstant(){}
}
