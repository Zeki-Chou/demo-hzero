package com.hand.demo.infra.util;

import com.hand.demo.infra.constant.PurchaseStatus;
import org.opensaml.xml.signature.P;

import java.util.Collections;

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
}
