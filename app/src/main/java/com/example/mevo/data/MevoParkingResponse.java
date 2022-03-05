package com.example.mevo.data;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class MevoParkingResponse {
    @SerializedName("data")
    JsonObject data;
    public JsonObject getData() {
        return data;
    }
}
