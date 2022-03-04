package com.example.mevo.utils;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import android.graphics.Color;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mevo.data.MevoResponse;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiFetcher {

    private final MevoApi mevoApi;
    public static final String VEHICLE_SOURCE_ID = "geojson-source-vehicle";
    public static final String PARKING_SOURCE_ID = "geojson-source-parking";
    public static final String VEHICLE_LAYER_ID = "vehicle-layer";
    public static final String PARKING_LAYER_ID = "parking-layer";

    public ApiFetcher(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl(MevoApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.mevoApi = retrofit.create(MevoApi.class);
    }

    public void getVehiclesSource(Style style) {
        Call<JsonObject> call = mevoApi.getWellingtonVehicles();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    FeatureCollection vehicleCollection = FeatureCollection.fromJson(response.body().get("data").toString());
                    GeoJsonSource geoJsonSourceVehicle = new GeoJsonSource(VEHICLE_SOURCE_ID, vehicleCollection.toJson());
                    style.addSource(geoJsonSourceVehicle);
                    style.addLayer(new SymbolLayer(VEHICLE_LAYER_ID, VEHICLE_SOURCE_ID).withProperties(
                            iconImage(vehicleCollection.features().get(0).properties().get("iconUrl").getAsString()),
                            iconSize(1f),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true),
                            iconOffset(new Float[] {0f, -7f})
                    ));
                }else {
                    System.out.println(response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getParkingSource(Style style) {
        Call<JsonObject> call = mevoApi.getWellingtonParking();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Feature feature = Feature.fromJson(response.body().get("data").toString());
                    Polygon parking = (Polygon) feature.geometry();
                    GeoJsonSource geoJsonSourceParking = new GeoJsonSource(PARKING_SOURCE_ID, parking.toJson());
                    style.addSource(geoJsonSourceParking);
                    FillLayer parkingPolygonFillLayer = new FillLayer(PARKING_LAYER_ID, PARKING_SOURCE_ID);
                    parkingPolygonFillLayer.setProperties(
                            PropertyFactory.fillColor(Color.parseColor(feature.properties().get("fill").getAsString())),
                            PropertyFactory.fillOpacity(Float.parseFloat(feature.properties().get("fill-opacity").getAsString()))

                    );
                    parkingPolygonFillLayer.setFilter(eq(literal("$type"), literal("Polygon")));
                    style.addLayer(parkingPolygonFillLayer);
                }else {
                    System.out.println(response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error has occurred", Toast.LENGTH_LONG).show();
            }
        });
    }

}
