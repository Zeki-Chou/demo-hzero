package com.hand.demo.infra.util;

import com.hand.demo.infra.constant.PurchaseStatus;

/**
 * Utils
 */
public class Utils {
    private Utils() {}

    public static boolean validPurchaseStatus(String status) {
        for (PurchaseStatus s: PurchaseStatus.values()) {
            if (s.name().equals(status)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}
