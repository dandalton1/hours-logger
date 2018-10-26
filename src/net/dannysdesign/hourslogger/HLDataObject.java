package net.dannysdesign.hourslogger;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        payment = ((double) duration().toMillis()) / 3600000.;
    }

    String humanReadableStartTime() {
        ZonedDateTime dateTime = startTime.atZone(ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    String humanReadableEndTime() {
        ZonedDateTime dateTime = endTime.atZone(ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    private Duration duration() {
        return Duration.between(startTime, endTime);
    }

    String humanReadableDuration() {
        Duration duration = duration(); // wow this line lmao
        if (duration.getSeconds() < 1) {
            return (duration.getNano() / 1000000) + "ms";
        } else {
            if (duration.getSeconds() > 60) {
                if (duration.getSeconds() > 3600) {
                    if (duration.getSeconds() > 86400) {
                        if (duration.getSeconds() > 31536000) {
                            return String.format("%.03f", duration.getSeconds() / 31536000.) + " years";
                        } else {
                            return String.format("%.03f", duration.getSeconds() / 86400.) + " days";
                        }
                    } else {
                        return String.format("%.03f", duration.getSeconds() / 3600.) + " hours";
                    }
                } else {
                    return String.format("%.03f", duration.getSeconds() / 60.) + " minutes";
                }
            } else {
                return duration.getSeconds() + "." + (duration.getNano() / 1000000) + " seconds";
            }
        }
    }
}