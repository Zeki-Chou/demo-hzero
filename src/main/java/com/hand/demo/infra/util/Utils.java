package com.hand.demo.infra.util;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvApplyHeaderConstant;
import com.hand.demo.infra.constant.PurchaseStatus;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utils
 */
public class Utils {
    private Utils() {}

    public static String generateNStringMasking(int length, String maskCharacter) {
        return String.join("", Collections.nCopies(length, maskCharacter));
    }
}
