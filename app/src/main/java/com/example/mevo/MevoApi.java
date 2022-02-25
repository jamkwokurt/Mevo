package com.example.mevo;

import com.mapbox.geojson.FeatureCollection;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MevoApi {
    String BASE_URL = "https://api.mevo.co.nz/public/";
    @GET("vehicles/wellington")
    Call<MevoData> getWellingtonVehicles();
    @GET("parking/wellington")
    Call<MevoData> getWellingtonParking();
}
