package com.github.dwursteisen.devoxx.scheduler;

import com.github.dwursteisen.devoxx.scheduler.api.DevoxxRestApi;
import com.github.dwursteisen.devoxx.scheduler.api.Slot;
import com.google.gson.Gson;
import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.rx.client.MongoClients;
import com.mongodb.async.rx.client.MongoCollection;
import com.mongodb.async.rx.client.MongoDatabase;
import com.mongodb.connection.ClusterSettings;
import org.bson.Document;
import retrofit.RestAdapter;
import rx.Observable;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

import java.net.UnknownHostException;
import java.util.Arrays;

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

    private static MongoDatabase buildMongoDb() throws UnknownHostException {
        ClusterSettings clusterSettings = ClusterSettings.builder()
                .hosts(Arrays.asList(new ServerAddress("localhost:27017")))
                .build();

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .clusterSettings(clusterSettings)
                .build();

        return MongoClients.create(clientSettings).getDatabase("devoxx");
    }

    public static void main(String[] args) throws UnknownHostException {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://cfp.devoxx.be/api/conferences/DevoxxBe2014")
                .build();

        DevoxxRestApi service = restAdapter.create(DevoxxRestApi.class);

        schedules = buildMongoDb().getCollection("schedules");

        Gson gson = new Gson();

        Observable.from("wednesday", "thursday", "friday")
                .flatMap(service::schedules)
                .flatMap((s) -> Observable.from(s.slots))
                .map(DataFeeder::updateId)
                .flatMap(DataFeeder::isNotAlreadyInDb)
                .map(gson::toJson)
                .map(Document::valueOf)
                .toList()
                .flatMap(schedules::insert)
                .subscribe();
    }


    private static Slot updateId(final Slot slot) {
        slot._id = slot.slotId;
        return slot;
    }

    private static Observable<Slot> isNotAlreadyInDb(final Slot slot) {
        return schedules.find(new Document("_id", slot._id))
                .one()
                .onErrorFlatMap((ex) -> null)
                .filter(s -> s != null)
                .map((d) -> slot);
    }
}
