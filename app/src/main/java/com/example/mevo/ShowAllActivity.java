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
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mevo.data.MevoVehicleData;
import com.example.mevo.data.MevoParkingResponse;
import com.example.mevo.data.MevoVehicleResponse;
import com.example.mevo.network.RetrofitClient;
import com.google.gson.JsonObject;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowAllActivity extends AppCompatActivity implements OnMapReadyCallback, OnCameraTrackingChangedListener {
    public static final String VEHICLE_SOURCE_ID = "geojson-source-vehicle";
    public static final String PARKING_SOURCE_ID = "geojson-source-parking";
    public static final String VEHICLE_LAYER_ID = "vehicle-layer";
    public static final String PARKING_FILL_LAYER_ID = "parking-fill-layer";
    public static final String PARKING_Line_LAYER_ID = "parking-line-layer";
    private static final String SAVED_STATE_CAMERA = "saved_state_camera";
    private static final String SAVED_STATE_RENDER = "saved_state_render";
    private static final String SAVED_STATE_LOCATION = "saved_state_location";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private String MAPBOX_ACCESS_TOKEN;

//    private Location lastLocation;
    private LocationComponent locationComponent;

    @CameraMode.Mode
    private int cameraMode = CameraMode.TRACKING;

    @RenderMode.Mode
    private int renderMode = RenderMode.NORMAL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MAPBOX_ACCESS_TOKEN = getResources().getString(R.string.mapbox_access_token);
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, MAPBOX_ACCESS_TOKEN);
        setContentView(R.layout.activity_main);
        // Check and use saved instance state in case of device rotation
        if (savedInstanceState != null) {
            cameraMode = savedInstanceState.getInt(SAVED_STATE_CAMERA);
            renderMode = savedInstanceState.getInt(SAVED_STATE_RENDER);
//            lastLocation = savedInstanceState.getParcelable(SAVED_STATE_LOCATION);
        }

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        // Check for (and request) the device location permission
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            mapView.getMapAsync(this);
        } else {
            permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> permissionsToExplain) {
                    Toast.makeText(ShowAllActivity.this,
                            R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPermissionResult(boolean granted) {
                    if (granted) {
                        mapView.getMapAsync(ShowAllActivity.this);
                    } else {
                        finish();
                    }
                }
            });
            permissionsManager.requestLocationPermissions(this);
        }
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        ShowAllActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {

            initButtons();
            updateLocationComponent();

        });
    }

    @SuppressLint("MissingPermission")
    public void updateLocationComponent(){
        // Retrieve and customize the Maps SDK's LocationComponent
        locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions
                        .builder(this, mapboxMap.getStyle())
                        .useDefaultLocationEngine(true)
                        .locationEngineRequest(new LocationEngineRequest.Builder(750)
                                .setFastestInterval(750)
                                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                                .build())
                        .build());

        locationComponent.setLocationComponentEnabled(true);
        locationComponent.addOnCameraTrackingChangedListener(this);
        locationComponent.setCameraMode(cameraMode);
    }

    public void initButtons() {
        Button vehiclesBtn = findViewById(R.id.btnV);
        Button parkingBtn = findViewById(R.id.btnP);
        vehiclesBtn.setOnClickListener(v -> {
            if (locationComponent == null) {
                return;
            }
            toggleVehicle();
        });
        parkingBtn.setOnClickListener(v -> {
            if (locationComponent == null) {
                return;
            }
            toggleParking();
        });
    }

    public void toggleVehicle() {
        Style style = mapboxMap.getStyle();

        if (style.getLayer(VEHICLE_LAYER_ID) == null) {
            this.showVehicles(style);
        } else {
            style.removeLayer(VEHICLE_LAYER_ID);
            style.removeSource(VEHICLE_SOURCE_ID);
        }
        Toast.makeText(this, R.string.showVehicles, Toast.LENGTH_LONG).show();
        changeMapZoom();
    }

    public void toggleParking() {
        Style style = mapboxMap.getStyle();
        if (style.getSource(PARKING_SOURCE_ID) == null) {
            this.showParking(style);
        } else {
            style.removeLayer(PARKING_FILL_LAYER_ID);
            style.removeLayer(PARKING_Line_LAYER_ID);
            style.removeSource(PARKING_SOURCE_ID);
        }
        Toast.makeText(this, R.string.showParking, Toast.LENGTH_LONG).show();
        changeMapZoom();
    }

    public void changeMapZoom(){
        CameraPosition position = new CameraPosition.Builder()
                .zoom(11) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder
        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 2000);
    }


    public void showVehicles(Style style) {
        Call<MevoVehicleResponse> call = RetrofitClient.getInstance().getMevoApi().getWellingtonVehicles();
        call.enqueue(new Callback<MevoVehicleResponse>() {

            @Override
            public void onResponse(Call<MevoVehicleResponse> call, Response<MevoVehicleResponse> response) {
                if (response.isSuccessful()) {
                    MevoVehicleResponse mevoResponse = response.body();
                    MevoVehicleData data = mevoResponse.getData();
                    List<JsonObject> jsonObjects = data.getVehicleFeatures();
                    List<Feature> vehicleFeaturesFromJson = new ArrayList<>();
                    for (JsonObject obj : jsonObjects) {
                        Feature vehicle = Feature.fromJson(obj.toString());
                        vehicleFeaturesFromJson.add(vehicle);
                    }
//                    FeatureCollection vehicleCollectionFromJson = FeatureCollection.fromFeatures(vehicleFeaturesFromJson);
                    GeoJsonSource geoJsonSourceVehiclefromJson = new GeoJsonSource(VEHICLE_SOURCE_ID, FeatureCollection.fromFeatures(vehicleFeaturesFromJson));
                    addVehicleLayer(style,geoJsonSourceVehiclefromJson);
                    if (style.getLayer(VEHICLE_LAYER_ID) != null) {

                    }
                }
            }

            @Override
            public void onFailure(Call<MevoVehicleResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showParking(Style style) {
        Call<MevoParkingResponse> call = RetrofitClient.getInstance().getMevoApi().getWellingtonParking();
        call.enqueue(new Callback<MevoParkingResponse>() {
            @Override
            public void onResponse(@NonNull Call<MevoParkingResponse> call, @NonNull Response<MevoParkingResponse> response) {
                if (response.isSuccessful()) {
                    MevoParkingResponse mevoResponse = response.body();
                    JsonObject parkingJsonObject = mevoResponse.getData();
                    Feature parkingFeatureFromJson = Feature.fromJson(parkingJsonObject.toString());
//                    Polygon polygonParking = (Polygon) parkingFeatureFromJson.geometry();
                    GeoJsonSource geoJsonSourceParkingFromJson = new GeoJsonSource(PARKING_SOURCE_ID, parkingFeatureFromJson.toJson());
                    addParkingLayer(style,geoJsonSourceParkingFromJson,parkingFeatureFromJson);
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

    public void addVehicleLayer(Style style, GeoJsonSource geoJsonSource){
        style.addSource(geoJsonSource);
        style.addImage("icon", BitmapFactory.decodeResource(
                ShowAllActivity.this.getResources(), R.drawable.icon));
//                    Picasso.get().load(vehicleFeaturesFromJson.get(0).properties().get("iconUrl").getAsString()).into(new Target() {
//                        @Override
//                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                            style.addImage("icon", bitmap);
//                        }
//
//                        @Override
//                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//
//                        }
//
//                        @Override
//                        public void onPrepareLoad(Drawable placeHolderDrawable) {}
//                    });
        style.addLayer(new SymbolLayer(VEHICLE_LAYER_ID, VEHICLE_SOURCE_ID).withProperties(
//                            vehicleCollectionFromJson.features().get(0).properties().get("iconUrl").getAsString()
                iconImage("icon"),
                iconSize(0.2f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[]{0f, -7f})
        ));
    }

    public void addParkingLayer(Style style, GeoJsonSource geoJsonSource, Feature feature){
        style.addSource(geoJsonSource);
        FillLayer parkingPolygonFillLayer = new FillLayer(PARKING_FILL_LAYER_ID, PARKING_SOURCE_ID);
        parkingPolygonFillLayer.setProperties(
                            PropertyFactory.fillColor(feature.properties().get("fill").getAsString()),
                PropertyFactory.fillOpacity(Float.parseFloat(feature.properties().get("fill-opacity").getAsString()))

        );
        parkingPolygonFillLayer.setFilter(eq(literal("$type"), literal("Polygon")));
        LineLayer lineLayer = new LineLayer(PARKING_Line_LAYER_ID, PARKING_SOURCE_ID);
        lineLayer.setProperties(
                PropertyFactory.lineColor("#f7590d"),
                PropertyFactory.lineWidth(feature.properties().get("stroke-width").getAsFloat()),
                PropertyFactory.lineOpacity(feature.properties().get("stroke-opacity").getAsFloat())

        );
        style.addLayer(parkingPolygonFillLayer);
        style.addLayer(lineLayer);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

        // Save LocationComponent-related settings to use once device rotation is finished
        outState.putInt(SAVED_STATE_CAMERA, cameraMode);
        outState.putInt(SAVED_STATE_RENDER, renderMode);
        if (locationComponent != null) {
            outState.putParcelable(SAVED_STATE_LOCATION, locationComponent.getLastKnownLocation());
        }
    }

    @Override
    public void onCameraTrackingDismissed() {

    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {

    }

    private List<Feature> getFeaturesInViewport(String layerName) {
        RectF rectF = new RectF(mapView.getLeft(),
                mapView.getTop(), mapView.getRight(), mapView.getBottom());
        return mapboxMap.queryRenderedFeatures(rectF, layerName);
    }

}
