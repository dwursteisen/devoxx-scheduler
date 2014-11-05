package com.github.dwursteisen.devoxx.scheduler.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by david.wursteisen on 05/11/2014.
 */
public class Room {

    @SerializedName("_id")
    public String roomId;
    public String name;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        if (name != null ? !name.equals(room.name) : room.name != null) return false;
        if (roomId != null ? !roomId.equals(room.roomId) : room.roomId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = roomId != null ? roomId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
