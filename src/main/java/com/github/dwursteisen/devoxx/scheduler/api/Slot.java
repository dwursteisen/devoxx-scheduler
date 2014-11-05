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
