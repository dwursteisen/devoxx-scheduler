package com.github.dwursteisen.devoxx.scheduler.model;

import com.github.dwursteisen.devoxx.scheduler.api.Room;
import com.github.dwursteisen.devoxx.scheduler.api.Slot;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by david.wursteisen on 05/11/2014.
 */
public class Day {
    public String name;
    public Map<Room, List<Slot>> planning = new HashMap<>();


    @Override
    public String toString() {

        Observable<String> planningObs = Observable.from(planning.entrySet())
                .flatMap((entry) -> Observable.from(entry.getValue()).reduce(entry.getKey().name + " ", this::appendSlot))
                .reduce("", (str, timePlanning) -> str + timePlanning + "\n");


        return Observable.just(String.format("=========== %s ===========\n", name))
                .concatWith(planningObs)
                .concatWith(Observable.just("\n"))
                .reduce("", (seed, value) -> seed + value)
                .toBlocking().single();
    }

    private String appendSlot(String buffer, Slot slot) {
        if (slot.isTalk()) {
            return buffer + String.format("-|>  (%s) %s ", slot.fromTime, slot.talk.title);
        } else if (slot.isBreak()) {
            return buffer + String.format("-|>  (%s) BREAK %s ", slot.fromTime, slot.breakObj.nameFR);
        } else {
            return buffer + String.format("-|>  (%s) %s ", slot.fromTime, slot.roomName);
        }
    }
}
