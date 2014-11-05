package com.github.dwursteisen.devoxx.scheduler.api;

import java.util.List;

/**
 * Created by david.wursteisen on 05/11/2014.
 */
public class Talk {
    public String talkType;
    public String talk;
    public String summaryAsHtml;
    public String id;
    public String title;
    public String summary;
    public List<Speaker> speakers;
    public String fromTime;
    public long toTimeMillis;
    public String toTime;
    public String roomName;
    public String slotId;
    public String day;

    @Override
    public String toString() {
        return "Talk{" +
                "talk='" + talk + '\'' +
                ", title='" + title + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
