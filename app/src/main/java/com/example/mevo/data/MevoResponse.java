package com.example.mevo.data;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

//@JsonClass(generateAdapter = true)
public class MevoResponse {
    @SerializedName("data")
    MevoData data;

    public MevoResponse(MevoData data){
        this.data = data;
    }

    public MevoData getData() {
        return data;
    }

}
