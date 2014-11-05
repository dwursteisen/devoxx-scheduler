package com.github.dwursteisen.devoxx.scheduler.api;

import com.google.gson.annotations.SerializedName;

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
    public long fromTimeMilis;
    public String toTime;
    public long toTimeMillis;
    @SerializedName("break")
    public Break breakObj;
    public Talk talk;


    public boolean isBreak() {
        return talk == null;
    }

    public boolean isTalk() {
        return !isBreak();
    }


    @Override
    public String toString() {
        return "Slot{" +
                "slotId='" + slotId + '\'' +
                ", talk=" + talk +
                '}';
    }
}
