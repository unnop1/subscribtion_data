package com.nt.subscribtion_data.util;


import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DateTime {

    public static final String getTimeStampNowStr(){
        // สร้าง DateTimeFormatter ด้วยรูปแบบที่ต้องการ
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // รับเวลาปัจจุบันในรูปแบบ LocalDateTime
        LocalDateTime now = LocalDateTime.now();

        // แปลงเวลาปัจจุบันเป็น string ในรูปแบบที่กำหนด
        String formattedDateTime = now.format(formatter);
        return formattedDateTime;
    }

    public static Timestamp getTimestampNowUTC(){
        LocalDateTime now = LocalDateTime.now();

        Instant instant = now.toInstant(ZoneOffset.UTC);

        // Convert Instant to Timestamp
        Timestamp timestampNowUTC = Timestamp.from(instant);
        return timestampNowUTC;
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

}