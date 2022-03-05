package com.example.mevo.network;

import com.example.mevo.data.MevoParkingResponse;
import com.example.mevo.data.MevoResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Api {
    String wgnVehicles = "vehicles/wellington";
    String wgnParking = "parking/wellington";
    String BASE_URL = "https://api.mevo.co.nz/public/";
    @GET(wgnVehicles)
    Call<MevoResponse> getWellingtonVehicles();
    @GET(wgnParking)
    Call<MevoParkingResponse> getWellingtonParking();
}
