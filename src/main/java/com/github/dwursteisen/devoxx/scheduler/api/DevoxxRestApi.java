package com.github.dwursteisen.devoxx.scheduler.api;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by david.wursteisen on 05/11/2014.
 */
public interface DevoxxRestApi {

    @GET("/schedules/{day}")
    Observable<Schedule> schedules(@Path("day") String day);

}
