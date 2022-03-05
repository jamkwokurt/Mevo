package com.example.mevo.data;

import com.google.gson.annotations.SerializedName;

//@JsonClass(generateAdapter = true)
public class MevoVehicleResponse {
    @SerializedName("data")
    MevoVehicleData data;

    public MevoVehicleResponse(MevoVehicleData data){
        this.data = data;
    }

    public MevoVehicleData getData() {
        return data;
    }

}
