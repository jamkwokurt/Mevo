package com.example.mevo.utils;

import com.example.mevo.data.MevoResponse;
import com.google.gson.JsonObject;
import com.mapbox.geojson.FeatureCollection;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MevoApi {
    String wgnVehicles = "vehicles/wellington";
    String wgnParking = "parking/wellington";
    String BASE_URL = "https://api.mevo.co.nz/public/";
    @GET(wgnVehicles)
    Call<MevoResponse> getWellingtonVehicles();
    @GET(wgnParking)
    Call<MevoResponse> getWellingtonParking();
}
