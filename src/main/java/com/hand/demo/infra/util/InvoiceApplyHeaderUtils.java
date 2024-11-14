package com.hand.demo.infra.util;

import com.hand.demo.infra.constant.InvApplyHeaderConstant;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;

import java.util.HashMap;
import java.util.Map;

public class InvoiceApplyHeaderUtils {
    private InvoiceApplyHeaderUtils() {}

    public static String generateInvoiceCode(CodeRuleBuilder codeRuleBuilder) {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("customSegment", "-");
        return codeRuleBuilder.generateCode(InvApplyHeaderConstant.RULE_CODE, variableMap);
    }
}
