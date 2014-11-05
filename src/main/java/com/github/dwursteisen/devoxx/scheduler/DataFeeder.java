package com.github.dwursteisen.devoxx.scheduler;

import com.github.dwursteisen.devoxx.scheduler.api.DevoxxRestApi;
import com.github.dwursteisen.devoxx.scheduler.api.Room;
import com.github.dwursteisen.devoxx.scheduler.api.Slot;
import com.google.gson.Gson;
import com.mongodb.WriteConcernResult;
import com.mongodb.async.rx.client.MongoCollection;
import com.mongodb.async.rx.client.MongoDatabase;
import org.bson.Document;
import retrofit.RestAdapter;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

/**
 * Created by david.wursteisen on 05/11/2014.
 */
public class DataFeeder {


    static {
        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                e.printStackTrace();
            }
        });
    }

    private static MongoCollection<Document> schedules;
    private static MongoCollection<Document> rooms;
    private static Gson gson = new Gson();

    private static Conf conf = new Conf();

    public static void main(String[] args) throws Exception {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://cfp.devoxx.be/api/conferences/DevoxxBe2014")
                .build();

        DevoxxRestApi service = restAdapter.create(DevoxxRestApi.class);

        MongoDatabase db = conf.buildMongoDb();
        schedules = db.getCollection("schedules");
        rooms = db.getCollection("rooms");


        ConnectableObservable<Slot> slots = Observable.from("monday", "tuesday", "wednesday", "thursday", "friday")
                .flatMap(service::schedules)
                .flatMap((s) -> Observable.from(s.slots))
                .map(DataFeeder::updateId).publish();

        writeSlots(slots).subscribe();
        writeRooms(slots).subscribe();


        slots.connect();
    }

    private static Observable<WriteConcernResult> writeRooms(final ConnectableObservable<Slot> slots) {
        return slots.map(DataFeeder::toRoom)
                .distinct((r) -> r.roomId)
                .map(gson::toJson)
                .map(Document::valueOf)
                .buffer(3)
                .flatMap(rooms::insert);
    }

    private static Room toRoom(final Slot slot) {
        Room room = new Room();
        room.name = slot.roomName;
        room.roomId = slot.roomId;
        return room;
    }

    private static Observable<WriteConcernResult> writeSlots(final ConnectableObservable<Slot> slots) {
        return slots
                .flatMap(DataFeeder::isNotAlreadyInDb)
                .doOnNext((s) -> System.out.println("NOT IN DB -> " + s))
                .map(gson::toJson)
                .map(Document::valueOf)
                .buffer(5)
                .flatMap(schedules::insert);
    }


    private static Slot updateId(final Slot slot) {
        slot._id = slot.slotId;
        return slot;
    }

    private static Observable<Slot> isNotAlreadyInDb(final Slot slot) {
        return schedules.find(new Document("_id", slot._id))
                .one()
                .onErrorFlatMap((ex) -> null)
                .filter(s -> s == null)
                .map((d) -> slot);
    }
}
