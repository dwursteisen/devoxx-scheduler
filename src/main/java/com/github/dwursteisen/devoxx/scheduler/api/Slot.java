package com.github.dwursteisen.devoxx.scheduler.api;

import com.google.gson.annotations.SerializedName;

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
        return Long.divideUnsigned(minutes * 960, TimeUnit.HOURS.toMinutes(8));
    }

    public Slot next;

    public long sizeNext() {
        if(next == null) {
            return 0;
        }
        final long duration = Long.valueOf(next.fromTimeMillis) - Long.valueOf(toTimeMillis);
        return computeDurationSize(duration);
    }

    public String displayName() {
        if(isBreak()) {
            return breakObj.nameEN;
        } else if(isTalk()) {
            return talk.title;
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
