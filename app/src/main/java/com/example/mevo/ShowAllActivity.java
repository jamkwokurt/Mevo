package com.example.mevo;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mevo.data.MevoData;
import com.example.mevo.data.MevoResponse;
import com.example.mevo.utils.ApiFetcher;
import com.example.mevo.network.RetrofitClient;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
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
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowAllActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private String MAPBOX_ACCESS_TOKEN;
    public static final String VEHICLE_SOURCE_ID = "geojson-source-vehicle";
    public static final String PARKING_SOURCE_ID = "geojson-source-parking";
    public static final String VEHICLE_LAYER_ID = "vehicle-layer";
    public static final String PARKING_LAYER_ID = "parking-layer";
    private ApiFetcher apiFetcher = new ApiFetcher();


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

    public void initButtons(){
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

    public void toggleVehicle(){
        Style style = mapboxMap.getStyle();

        if (style.getLayer(VEHICLE_LAYER_ID) == null) {
            this.getVehiclesSource(style);
        }else {
            style.removeLayer(VEHICLE_LAYER_ID);
            style.removeSource(VEHICLE_SOURCE_ID);
        }
        Toast.makeText(this, R.string.showVehicles, Toast.LENGTH_LONG).show();
    }

    public void toggleParking(){
        Style style = mapboxMap.getStyle();
        if (style.getSource(PARKING_SOURCE_ID) == null) {
            this.getParkingSource(style);
        }else {
            style.removeLayer(PARKING_LAYER_ID);
            style.removeSource(PARKING_SOURCE_ID);
        }
        Toast.makeText(this, R.string.showParking, Toast.LENGTH_LONG).show();
    }


    public void getVehiclesSource(Style style) {
        Call<MevoResponse> call = RetrofitClient.getInstance().getMevoApi().getWellingtonVehicles();
        call.enqueue(new Callback<MevoResponse>() {

            @Override
            public void onResponse(Call<MevoResponse> call, Response<MevoResponse> response) {
                if (response.isSuccessful()) {
                    MevoResponse mevoResponse = response.body();
                    MevoData data = mevoResponse.getData();
                    FeatureCollection vehicleCollection = FeatureCollection.fromJson(data.getVehicleFeatures().toString());
                    GeoJsonSource geoJsonSourceVehicle = new GeoJsonSource(VEHICLE_SOURCE_ID, vehicleCollection.toJson());
                    style.addSource(geoJsonSourceVehicle);
                    style.addLayer(new SymbolLayer(VEHICLE_LAYER_ID, VEHICLE_SOURCE_ID).withProperties(
                            iconImage(vehicleCollection.features().get(0).properties().get("iconUrl").getAsString()),
                            iconSize(1f),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true),
                            iconOffset(new Float[]{0f, -7f})
                    ));
                }
            }

            @Override
            public void onFailure(Call<MevoResponse> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getParkingSource(Style style) {
        Call<MevoResponse> call = RetrofitClient.getInstance().getMevoApi().getWellingtonParking();
        call.enqueue(new Callback<MevoResponse>() {
            @Override
            public void onResponse(@NonNull Call<MevoResponse> call, @NonNull Response<MevoResponse> response) {
                if (response.isSuccessful()) {
                    MevoResponse mevoResponse = response.body();
                    MevoData data = mevoResponse.getData();
                    Feature feature = Feature.fromJson(data.getParkingGeometry().toString());
                    GeoJsonSource geoJsonSourceParking = new GeoJsonSource(PARKING_SOURCE_ID, feature.toJson());
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
            public void onFailure(@NonNull Call<MevoResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"})
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
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
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
    @SuppressWarnings( {"MissingPermission"})
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
}
