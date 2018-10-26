package net.dannysdesign.hourslogger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

class HLDataObject {
    HLDataObject() { }

    ArrayList<HLClientObject> clients = new ArrayList<>();
}

class HLClientObject {
    HLClientObject() { }

    String name;
    double rate = Double.MIN_VALUE;
    ArrayList<HLWorkObject> workObjects = new ArrayList<>();

    @Override
    public String toString() {
        return name;
    }
}

class HLWorkObject {
    HLWorkObject() { }

    Instant startTime;
    Instant endTime;
    String description;
    double payment;

    void calculate() {
        long millis = endTime.toEpochMilli() - startTime.toEpochMilli();
        payment = ((double) millis) / 3600000.;
    }

    String humanReadableStartTime() {
        ZonedDateTime dateTime = startTime.atZone(ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    String humanReadableEndTime() {
        ZonedDateTime dateTime = endTime.atZone(ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}