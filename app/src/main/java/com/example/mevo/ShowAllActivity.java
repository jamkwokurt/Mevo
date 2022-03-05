package com.example.mevo;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;


import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mevo.data.MevoVehicleData;
import com.example.mevo.data.MevoParkingResponse;
import com.example.mevo.data.MevoVehicleResponse;
import com.example.mevo.network.RetrofitClient;
import com.google.gson.JsonObject;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowAllActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {
    public static final String VEHICLE_SOURCE_ID = "geojson-source-vehicle";
    public static final String PARKING_SOURCE_ID = "geojson-source-parking";
    public static final String VEHICLE_LAYER_ID = "vehicle-layer";
    public static final String PARKING_LAYER_ID = "parking-layer";
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private String MAPBOX_ACCESS_TOKEN;

    private LocationEngine locationEngine;
    private ShowAllActivityLocationCallback callback =
            new ShowAllActivityLocationCallback(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MAPBOX_ACCESS_TOKEN = getResources().getString(R.string.mapbox_access_token);
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, MAPBOX_ACCESS_TOKEN);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        ShowAllActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
                initButtons();
            }
        });
    }

    public void initButtons() {
        Button vehiclesBtn = findViewById(R.id.btnV);
        Button parkingBtn = findViewById(R.id.btnP);
        vehiclesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleVehicle();
            }
        });
        parkingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleParking();
            }
        });
    }

    public void toggleVehicle() {
        Style style = mapboxMap.getStyle();

        if (style.getLayer(VEHICLE_LAYER_ID) == null) {
            this.getVehiclesSource(style);
        } else {
            style.removeLayer(VEHICLE_LAYER_ID);
            style.removeSource(VEHICLE_SOURCE_ID);
        }
        Toast.makeText(this, R.string.showVehicles, Toast.LENGTH_LONG).show();
    }

    public void toggleParking() {
        Style style = mapboxMap.getStyle();
        if (style.getSource(PARKING_SOURCE_ID) == null) {
            this.getParkingSource(style);
        } else {
            style.removeLayer(PARKING_LAYER_ID);
            style.removeSource(PARKING_SOURCE_ID);
        }
        Toast.makeText(this, R.string.showParking, Toast.LENGTH_LONG).show();
    }


    public void getVehiclesSource(Style style) {
        Call<MevoVehicleResponse> call = RetrofitClient.getInstance().getMevoApi().getWellingtonVehicles();
        call.enqueue(new Callback<MevoVehicleResponse>() {

            @Override
            public void onResponse(Call<MevoVehicleResponse> call, Response<MevoVehicleResponse> response) {
                if (response.isSuccessful()) {
                    MevoVehicleResponse mevoResponse = response.body();
                    MevoVehicleData data = mevoResponse.getData();

                    //Edited
                    List<JsonObject> jsonObjects = data.getVehicleFeatures();
                    List<Feature> vehicleFeaturesFromJson = new ArrayList<>();
                    for (JsonObject obj : jsonObjects) {
                        Feature vehicle = Feature.fromJson(obj.toString());
                        vehicleFeaturesFromJson.add(vehicle);
                    }
                    FeatureCollection vehicleCollectionFromJson = FeatureCollection.fromFeatures(vehicleFeaturesFromJson);
                    GeoJsonSource geoJsonSourceVehiclefromJson = new GeoJsonSource(VEHICLE_SOURCE_ID, FeatureCollection.fromFeatures(vehicleFeaturesFromJson));
                    style.addSource(geoJsonSourceVehiclefromJson);
                    style.addImage("icon", BitmapFactory.decodeResource(
                            ShowAllActivity.this.getResources(), R.drawable.icon));
                    style.addLayer(new SymbolLayer(VEHICLE_LAYER_ID, VEHICLE_SOURCE_ID).withProperties(
//                            vehicleCollectionFromJson.features().get(0).properties().get("iconUrl").getAsString()
                            iconImage("icon"),
                            iconSize(0.2f),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true),
                            iconOffset(new Float[]{0f, -7f})
                    ));
                    Log.d("vehicle", "layer added");
                }
            }

            @Override
            public void onFailure(Call<MevoVehicleResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getParkingSource(Style style) {
        Call<MevoParkingResponse> call = RetrofitClient.getInstance().getMevoApi().getWellingtonParking();
        call.enqueue(new Callback<MevoParkingResponse>() {
            @Override
            public void onResponse(@NonNull Call<MevoParkingResponse> call, @NonNull Response<MevoParkingResponse> response) {
                if (response.isSuccessful()) {
                    MevoParkingResponse mevoResponse = response.body();
                    JsonObject parkingJsonObject = mevoResponse.getData();

                    //edited
                    Feature parkingFeatureFromJson = Feature.fromJson(parkingJsonObject.toString());
                    Polygon polygonParking = (Polygon) parkingFeatureFromJson.geometry();
                    GeoJsonSource geoJsonSourceParkingFromJson = new GeoJsonSource(PARKING_SOURCE_ID, parkingFeatureFromJson.toJson());
                    style.addSource(geoJsonSourceParkingFromJson);
                    FillLayer parkingPolygonFillLayer = new FillLayer(PARKING_LAYER_ID, PARKING_SOURCE_ID);
                    parkingPolygonFillLayer.setProperties(
//                            PropertyFactory.fillColor(parkingFeatureFromJson.properties().get("fill").getAsString()),
                            PropertyFactory.fillColor("#cfe2f3"),

                            PropertyFactory.fillOpacity(Float.parseFloat(parkingFeatureFromJson.properties().get("fill-opacity").getAsString()))

                    );
                    parkingPolygonFillLayer.setFilter(eq(literal("$type"), literal("Polygon")));
                    LineLayer lineLayer = new LineLayer("parkingLineLayer", PARKING_SOURCE_ID);
                    lineLayer.setProperties(
                            PropertyFactory.lineColor("#f7590d"),
                            PropertyFactory.lineWidth(parkingFeatureFromJson.properties().get("stroke-width").getAsFloat()),
                            PropertyFactory.lineOpacity(parkingFeatureFromJson.properties().get("stroke-opacity").getAsFloat())

                    );
                    style.addLayer(parkingPolygonFillLayer);
                    style.addLayer(lineLayer);
                    Log.d("parking", "layer added");
                } else {
                    System.out.println(response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MevoParkingResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private static class ShowAllActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<ShowAllActivity> activityWeakReference;

        ShowAllActivityLocationCallback(ShowAllActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            ShowAllActivity activity = activityWeakReference.get();

            if (activity != null) {
                List<Location> locations = result.getLocations();

                if (locations == null) {
                    return;
                }

                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(locations, true);
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception e) {

        }
    }
}
