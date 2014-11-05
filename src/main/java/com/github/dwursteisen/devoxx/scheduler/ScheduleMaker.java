package com.github.dwursteisen.devoxx.scheduler;

import com.github.dwursteisen.devoxx.scheduler.api.Room;
import com.github.dwursteisen.devoxx.scheduler.api.Slot;
import com.github.dwursteisen.devoxx.scheduler.model.Day;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.Gson;
import com.mongodb.async.rx.client.MongoCollection;
import com.mongodb.async.rx.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonWriter;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.OnErrorThrowable;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * Created by david.wursteisen on 05/11/2014.
 */
public class ScheduleMaker {

    private static final Conf conf = new Conf();
    private static MongoCollection<Document> rooms;
    private static MongoCollection<Document> schedules;

    private static Gson gson = new Gson();
    private static Observable<Room> roomObservable;

    public static void main(String[] args) throws UnknownHostException, InterruptedException {

        MongoDatabase db = conf.buildMongoDb();
        rooms = db.getCollection("rooms");
        schedules = db.getCollection("schedules");

        roomObservable = rooms.find(new Document())
                .forEach()
                .map(ScheduleMaker::toJson)
                .map((json) -> gson.fromJson(json, Room.class))
                .toSortedList((r1, r2) -> r1.name.compareTo(r2.name))
                .flatMap(Observable::from)
                .cache();

        final Observable<List<Day>> days = Observable.from("monday", "tuesday", "wednesday", "thursday", "friday")
                .flatMap(ScheduleMaker::buildPlanningByDay)
                .toList();


        final Observable<Mustache> mustacheObservable = Observable.create(new MustacheOnSubscribe("template/index.html"));

        final Observable<HashMap<String, Object>> asMap = days.map((d) -> {
            final HashMap<String, Object> map = new HashMap<>();
            map.put("days", d);
            return map;
        });

        mustacheObservable.zip(asMap, (mustache, mapDays) -> mustache.execute(new StringWriter(), mapDays))
                .map((w) -> {
                    try {
                        w.flush();
                        return w.toString();
                    } catch (IOException e) {
                        throw OnErrorThrowable.from(e);
                    }
                })
                .doOnNext((str) -> {
                    try {
                        Files.write(Paths.get("./index.html"), str.getBytes());
                    } catch (IOException e) {
                        throw OnErrorThrowable.from(e);
                    }
                })
                .toBlocking()
                .forEach((n) -> {
                });


    }

    static {
        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                e.printStackTrace();
            }
        });
    }

    private static Observable<Day> buildPlanningByDay(final String day) {
        Observable<Slot> slots = schedules.find(new Document("day", day))
                .forEach()
                .map(ScheduleMaker::toJson)
                .map((json) -> gson.fromJson(json, Slot.class))
                .doOnNext(System.out::println)
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
            for (Map.Entry<Room, List<Slot>> entry : p.entrySet()) {
                Day.Planning forMustache = new Day.Planning();
                forMustache.room = entry.getKey();
                forMustache.slots = entry.getValue();
                d.planning.add(forMustache);
            }
            return d;
        });
    }

    private static Observable<List<Slot>> groupByRoom(final Room room, final Observable<Slot> slots) {
        return slots.filter((s) -> Objects.equals(s.roomId, room.roomId))
                .doOnNext(System.err::println)
                .toSortedList((s1, s2) -> Long.compare(Long.valueOf(s1.fromTimeMillis), Long.valueOf(s2.fromTimeMillis)));
    }

    private static String toJson(final Document document) {
        StringWriter writer = new StringWriter();
        new DocumentCodec().encode(new JsonWriter(writer), document, EncoderContext.builder().build());
        return writer.toString();
    }

    private static class MustacheOnSubscribe implements Observable.OnSubscribe<Mustache> {

        private final String template;

        public MustacheOnSubscribe(String template) {
            this.template = template;
        }

        @Override
        public void call(Subscriber<? super Mustache> subscriber) {
            try {
                MustacheFactory mf = new DefaultMustacheFactory();
                Mustache mustache = mf.compile(template);
                subscriber.onNext(mustache);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        }
    }
}
