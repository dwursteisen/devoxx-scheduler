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

    public int asInt() {
        final Observable<String> days = Observable.from("monday", "tuesday", "wednesday", "thursday", "friday");
        return days.scan(0, (seed, value) -> seed + 1).zip(days, Pair<Integer, String>::new)
                .filter((p) -> p.two.equals(name))
                .map((p) -> p.one)
                .toBlocking()
                .single();
    }


    private static class Pair<T, U> {
        private T one;
        private U two;

        public Pair(T one, U two) {
            this.one = one;
            this.two = two;
        }
    }

    public static class Planning {
        public Room room;
        public List<Slot> slots;
    }



}
