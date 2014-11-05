package com.github.dwursteisen.devoxx.scheduler;

import com.github.dwursteisen.devoxx.scheduler.api.Room;
import com.github.dwursteisen.devoxx.scheduler.api.Slot;
import com.github.dwursteisen.devoxx.scheduler.model.Day;
import com.google.gson.Gson;
import com.mongodb.async.rx.client.MongoCollection;
import com.mongodb.async.rx.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonWriter;
import rx.Observable;

import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by david.wursteisen on 05/11/2014.
 */
public class ScheduleMaker {

    private static final Conf conf = new Conf();
    private static MongoCollection<Document> rooms;
    private static MongoCollection<Document> schedules;

    private static Gson gson = new Gson();
    private static Observable<Room> roomObservable;

    public static void main(String[] args) throws UnknownHostException {

        MongoDatabase db = conf.buildMongoDb();
        rooms = db.getCollection("rooms");
        schedules = db.getCollection("schedules");

        roomObservable = rooms.find(new Document())
                .forEach()
                .map(ScheduleMaker::toJson)
                .map((json) -> gson.fromJson(json, Room.class))
                .cache();

        Observable.from("monday", "tuesday", "wednesday", "thursday", "friday")
                .flatMap(ScheduleMaker::buildPlanningByDay)
                .map(Day::toString)
                .reduce("", (seed, value) -> seed + value + "\n~~~~~~~~~\n")
                .toBlocking()
                .forEach(System.out::println);

    }

    private static Observable<Day> buildPlanningByDay(final String day) {
        Observable<Slot> slots = schedules.find(new Document("day", day))
                .forEach()
                .map(ScheduleMaker::toJson)
                .map((json) -> gson.fromJson(json, Slot.class))
                .cache();


        Observable<Map<Room, List<Slot>>> planning = roomObservable.flatMap((room) -> groupByRoom(room, slots).reduce(new HashMap<Room, List<Slot>>(), (seed, value) -> {
            seed.put(room, value);
            return seed;
        })).reduce(new HashMap<>(), (seed, value) -> {
            seed.putAll(value);
            return seed;
        });


        Day model = new Day();
        model.name = day;


        return Observable.just(model).zip(planning, (d, p) -> {
            d.planning.putAll(p);
            return d;
        });
    }

    private static Observable<List<Slot>> groupByRoom(final Room room, final Observable<Slot> slots) {
        return slots.filter((s) -> Objects.equals(s.roomId, room.roomId))
                .toSortedList((s1, s2) -> Long.compare(Long.valueOf(s1.fromTimeMillis), Long.valueOf(s2.fromTimeMillis)));
    }

    private static String toJson(final Document document) {
        StringWriter writer = new StringWriter();
        new DocumentCodec().encode(new JsonWriter(writer), document, EncoderContext.builder().build());
        return writer.toString();
    }
}
