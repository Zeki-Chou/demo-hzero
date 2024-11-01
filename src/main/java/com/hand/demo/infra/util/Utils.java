package com.hand.demo.infra.util;

/**
 * Utils
 */
public class Utils {
    private Utils() {}

    public static String desensitizeString(String text) {
        return text.replaceAll(".", "*");
    }

    public static String desensitizeString(String text, Integer start) {
        if(text.length() < 3) return text;

        return desensitizeString(text, start, false);
    }

    public static String desensitizeString(String text, Integer start, boolean fromStart) {
        if(text.length() < 3) return text;

        StringBuilder desensitizedString = new StringBuilder();
        if (fromStart) {
            for (int i = 0; i < start; i++) {
                desensitizedString.append("*");
            }
            return desensitizedString + text.substring(start);
        } else {
            for (int i = start; i < text.length(); i++) {
                desensitizedString.append("*");
            }
            return text.substring(0, start) + desensitizedString;
        }
    }
}
