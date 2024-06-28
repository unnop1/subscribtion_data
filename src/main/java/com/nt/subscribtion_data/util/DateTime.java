package com.nt.subscribtion_data.util;


import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

public class DateTime {

    public static final String getTimeStampNowStr(){
        // สร้าง DateTimeFormatter ด้วยรูปแบบที่ต้องการ
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        // รับเวลาปัจจุบันในรูปแบบ LocalDateTime
        LocalDateTime now = LocalDateTime.now();

        // แปลงเวลาปัจจุบันเป็น string ในรูปแบบที่กำหนด
        String formattedDateTime = now.format(formatter);
        return formattedDateTime;
    }

    public static Timestamp getTimestampNowUTC(){
        Instant instant = Instant.now();
        return Timestamp.from(instant);
    }

    public static final String getTriggerTimeStampNow(){
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                    .appendLiteral('Z')
                    .toFormatter();

        ZonedDateTime now = ZonedDateTime.now();

        String formattedDateTime = now.format(formatter);

        return formattedDateTime;
    }

    public static final String addZeroConvertISODate(String dateStr) {
        // "2001-01-07T10:00:00Z"
        String[] parts = dateStr.split(":");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            if (lastPart.endsWith("Z")) {
                if (lastPart.equals("00Z")) {
                    lastPart = "00.000Z";
                } else if (!lastPart.contains(".")) {
                    lastPart = lastPart.substring(0, lastPart.length() - 1) + ".000Z";
                }
                parts[parts.length - 1] = lastPart;
                return String.join(":", parts);
            }
        }
        return dateStr;
    }

}