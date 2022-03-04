package com.example.mevo.data;


import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.mapbox.geojson.Feature;

import java.util.List;

public class MevoData {
    @SerializedName("features")
    List<JsonObject> features;

    @SerializedName("geometry")
    JsonObject geometry;

    public List<JsonObject> getVehicleFeatures() {
        return features;
    }

    public JsonObject getParkingGeometry() {
        return geometry;
    }
}
