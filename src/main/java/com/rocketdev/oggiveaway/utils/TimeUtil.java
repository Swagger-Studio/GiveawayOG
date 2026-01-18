package com.rocketdev.oggiveaway.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    public static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static ZonedDateTime getCurrentIST() {
        return ZonedDateTime.now(IST_ZONE);
    }

    public static boolean isSameMinute(ZonedDateTime now, String targetStr) {
        try {
            // Target format: dd/MM/yyyy HH:mm
            String nowStr = now.format(FORMATTER);
            return nowStr.equals(targetStr);
        } catch (Exception e) {
            return false;
        }
    }
}