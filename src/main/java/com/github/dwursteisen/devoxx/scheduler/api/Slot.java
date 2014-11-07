package com.github.dwursteisen.devoxx.scheduler.api;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

/**
 * Created by david.wursteisen on 05/11/2014.
 */
public class Slot {
    public String _id;
    public String slotId;
    public String day;
    public String roomId;
    public String roomName;
    public String fromTime;
    public String fromTimeMillis;
    public String toTime;
    public String toTimeMillis;
    @SerializedName("break")
    public Break breakObj;
    public Talk talk;
    public Slot next;
    public Slot prev;

    public boolean isBreak() {
        return breakObj != null;
    }

    public boolean isTalk() {
        return talk != null;
    }

    public long size() {
        final long duration = Long.valueOf(toTimeMillis) - Long.valueOf(fromTimeMillis);
        return computeDurationSize(duration);

    }

    private long computeDurationSize(long duration) {
        // 8h = 1024px
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        return Long.divideUnsigned(minutes * 1048, TimeUnit.HOURS.toMinutes(8));
    }

    public long sizeNext() {
        if (next == null) {
            return 0;
        }
        final long duration = Long.valueOf(next.fromTimeMillis) - Long.valueOf(toTimeMillis);
        return computeDurationSize(duration);
    }

    public long sizePrev() {
        if (prev == null) {

            Instant instant = Instant.ofEpochMilli(Long.valueOf(fromTimeMillis));

            LocalDateTime now = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                    .withHour(7)
                    .withMinute(0);

            long l = Long.valueOf(fromTimeMillis) - now.toInstant(ZoneOffset.UTC).toEpochMilli();
            return computeDurationSize(l);
        }
        final long duration = Long.valueOf(fromTimeMillis) - Long.valueOf(prev.toTimeMillis);
        long durationSize = computeDurationSize(duration);
        return durationSize;
    }

    public String linkTo() {
        if (isTalk()) {
            return String.format("http://cfp.devoxx.be/2014/talk/%s", talk.id);
        } else {
            return null;
        }
    }
    public String className() {
        if (isTalk()) {
            if ("University".equals(talk.talkType)) {
                return "bg-primary";
            } else if ("Tools-in-Action".equals(talk.talkType)) {
                return "bg-success";
            } else if ("Quickie".equals(talk.talkType)) {
                return "bg-success";
            } else if ("Conference".equals(talk.talkType)) {
                return "bg-info";
            } else if ("Hand's on Labs".equals(talk.talkType)) {
                return "bg-warning";
            } else if ("Keynote".equals(talk.talkType)) {
                return "bg-danger";
            } else if ("BOF (Bird of a Feather)".equals(talk.talkType)) {
                return "bg-danger";
            } else if ("Startup presentation".equals(talk.talkType)) {
                return "bg-success";
            } else {
                return "";
            }
        } else {
            return "";
        }
    }


    public String style() {
        return String.format("height: %dpx; border: 1px solid black; margin-top: %dpx;", size(), sizePrev());
    }

    public String displayName() {
        if (isBreak()) {
            return String.format("(%s) %s", fromTime, breakObj.nameEN);
        } else if (isTalk()) {
            return String.format("(%s) %s", fromTime, talk.title);
        } else {
            return "??";
        }
    }

    @Override
    public String toString() {
        return "Slot{" +
                "slotId='" + slotId + '\'' +
                ", talk=" + talk +
                '}';
    }
}
