package com.hand.demo.infra.util;

import java.util.Calendar;
import java.util.Date;

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

    public static Date setToStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date setToEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
}
