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
        Call<MevoResponse> call = mevoApi.getWellingtonVehicles();
        call.enqueue(new Callback<MevoResponse>() {
            @Override
            public void onResponse(@NonNull Call<MevoResponse> call, @NonNull Response<MevoResponse> response) {
                if (response.isSuccessful()) {
                    MevoResponse mevoResponse = response.body();
                    String data = mevoResponse.getData().toString();
                    FeatureCollection vehicleCollection = FeatureCollection.fromJson(data);
                    if (vehicleCollection.features() != null) {
                        for (Feature vehicle : vehicleCollection.features()) {
                            if (vehicle.geometry() instanceof Point) {
                                GeoJsonSource geoJsonSourceVehicle = new GeoJsonSource(VEHICLE_SOURCE_ID, vehicle);
                                //move to main activity
                                style.addSource(geoJsonSourceVehicle);
                                style.addLayer(new SymbolLayer(VEHICLE_LAYER_ID, VEHICLE_SOURCE_ID).withProperties(
                                        iconImage(vehicle.properties().get("iconUrl").getAsString()),
                                        iconSize(1f),
                                        iconAllowOverlap(true),
                                        iconIgnorePlacement(true),
                                        iconOffset(new Float[] {0f, -7f})
                                ));
                            }
                        }
                    }
//                        for(MevoFeature feature : features){
//                            List<String> coordinates = feature.getGeo().getCoordinates();
//                            Double lng = Double.valueOf(coordinates.get(0));
//                            Double lat = Double.valueOf(coordinates.get(1));
//                            List<Point>points = new ArrayList<>();
//                            points.add(Point.fromLngLat(lng,lat));
//                            LineString lineString = LineString.fromLngLats(points);
//                            Feature featureFromString = Feature.fromGeometry(lineString);
//                            GeoJsonSource geoJsonSource = new GeoJsonSource("geojson-source", featureFromString);
//                            style.addSource(geoJsonSource);
//                        }

                }else {
                    System.out.println(response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MevoResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getParkingSource(Style style) {
        Call<MevoResponse> call = mevoApi.getWellingtonParking();
        call.enqueue(new Callback<MevoResponse>() {
            @Override
            public void onResponse(@NonNull Call<MevoResponse> call, @NonNull Response<MevoResponse> response) {
                if (response.isSuccessful()) {
                    MevoResponse mevoResponse = response.body();
                    String data = mevoResponse.getData().toString();
                    Feature feature = Feature.fromJson(data);
                    if (feature.geometry() instanceof Polygon) {
                        Polygon parking = (Polygon) feature.geometry();
                        GeoJsonSource geoJsonSourceParking = new GeoJsonSource(PARKING_SOURCE_ID, parking);
                        //move to main activity
                        style.addSource(geoJsonSourceParking);
                        FillLayer parkingPolygonFillLayer = new FillLayer(PARKING_LAYER_ID, PARKING_SOURCE_ID);
                        parkingPolygonFillLayer.setProperties(
                                PropertyFactory.fillColor(Color.parseColor(feature.properties().get("fill").getAsString())),
                                PropertyFactory.fillOpacity(Float.parseFloat(feature.properties().get("fill-opacity").getAsString()))

                        );
                        parkingPolygonFillLayer.setFilter(eq(literal("$type"), literal("Polygon")));
                        style.addLayer(parkingPolygonFillLayer);
                    }
                }else {
                    System.out.println(response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MevoResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_LONG).show();
            }
        });
    }

}
