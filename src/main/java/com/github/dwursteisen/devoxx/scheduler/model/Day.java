package com.github.dwursteisen.devoxx.scheduler.model;

import com.github.dwursteisen.devoxx.scheduler.api.Room;
import com.github.dwursteisen.devoxx.scheduler.api.Slot;
import rx.Observable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by david.wursteisen on 05/11/2014.
 */
public class Day {
    public String name;
    public List<Planning> planning = new LinkedList<>();


    public static class Planning {
        public Room room;
        public List<Slot> slots;
    }



}
