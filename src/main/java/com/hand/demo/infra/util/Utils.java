package com.hand.demo.infra.util;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.PurchaseStatus;

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

    public static boolean validPurchaseStatus(String status) {
        for (PurchaseStatus s: PurchaseStatus.values()) {
            if (s.name().equals(status)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * calculate the total, tax, and exclude tax amount from the invoice line object
     * @param invoiceApplyLine invoice line object
     * @return new object with calculated amount
     */
    public static InvoiceApplyLine calculateAmountLine(InvoiceApplyLine invoiceApplyLine) {
        BigDecimal lineTotalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
        BigDecimal lineTaxAmount = lineTotalAmount.multiply(invoiceApplyLine.getTaxRate());
        BigDecimal lineExcludeTaxAmount = lineTotalAmount.subtract(lineTaxAmount);

        invoiceApplyLine.setTotalAmount(lineTotalAmount);
        invoiceApplyLine.setTaxAmount(lineTaxAmount);
        invoiceApplyLine.setExcludeTaxAmount(lineExcludeTaxAmount);

        return invoiceApplyLine;
    }
}
