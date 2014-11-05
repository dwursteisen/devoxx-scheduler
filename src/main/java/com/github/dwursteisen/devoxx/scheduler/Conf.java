package com.github.dwursteisen.devoxx.scheduler;

import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.rx.client.MongoClients;
import com.mongodb.async.rx.client.MongoDatabase;
import com.mongodb.connection.ClusterSettings;

import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by david.wursteisen on 05/11/2014.
 */
public class Conf {
    public MongoDatabase buildMongoDb() throws UnknownHostException {
        ClusterSettings clusterSettings = ClusterSettings.builder()
                .hosts(Arrays.asList(new ServerAddress("localhost:27017")))
                .build();

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .clusterSettings(clusterSettings)
                .build();

        return MongoClients.create(clientSettings).getDatabase("devoxx");
    }
}
