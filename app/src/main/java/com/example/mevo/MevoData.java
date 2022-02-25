package com.example.mevo;

import com.google.gson.annotations.SerializedName;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;

public class MevoData {
    @SerializedName("type")
    String type;
    @SerializedName("features")
    FeatureCollection collection;

    public MevoData(String type, FeatureCollection collection){
        this.type = type;
        this.collection = collection;
    }
}
