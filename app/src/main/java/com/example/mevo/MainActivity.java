package com.example.mevo;


import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private String MAPBOX_ACCESS_TOKEN = "";
    private String vehicleSourceId = "geojson-source-vehicle";
    private String parkingSourceId = "geojson-source-parking";
    private Style style;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MAPBOX_ACCESS_TOKEN = getResources().getString(R.string.mapbox_access_token);
        // Set up a standard MapBox map
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, MAPBOX_ACCESS_TOKEN);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                getVehiclesSource(style);
                getParkingSource(style);
                addPolygonLayer(style);
                addPointsLayer(style);
            }
        });
    }

    private void getVehiclesSource(Style style) {
        GeoJsonSource geoJsonSource = null;
        Call<MevoData> call = RetrofitClient.getInstance().getMevoApi().getWellingtonVehicles();
        call.enqueue(new Callback<MevoData>() {
            @Override
            public void onResponse(@NonNull Call<MevoData> call, @NonNull Response<MevoData> response) {
                assert response.body() != null;
                FeatureCollection collection = response.body().collection;
                String featuresJson = collection.toJson();
                Feature feature = Feature.fromJson(featuresJson);
                GeoJsonSource geoJsonSource = new GeoJsonSource(vehicleSourceId, feature);
                style.addSource(geoJsonSource);
            }

            @Override
            public void onFailure(@NonNull Call<MevoData> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getParkingSource(Style style) {
        GeoJsonSource geoJsonSource = null;
        Call<MevoData> call = RetrofitClient.getInstance().getMevoApi().getWellingtonParking();
        call.enqueue(new Callback<MevoData>() {
            @Override
            public void onResponse(@NonNull Call<MevoData> call, @NonNull Response<MevoData> response) {
                assert response.body() != null;
                FeatureCollection collection = response.body().collection;
                String featuresJson = collection.toJson();
                Feature parkingFeature = Feature.fromJson(featuresJson);
                Polygon polygonParking = (Polygon) parkingFeature.geometry();
                assert polygonParking != null;
                polygonParking.coordinates().remove(0);
                GeoJsonSource geoJsonSource = new GeoJsonSource(parkingSourceId, parkingFeature);
                style.addSource(geoJsonSource);
                style.addLayer(new SymbolLayer("parkingLayer", "parkingArea"));
            }

            @Override
            public void onFailure(@NonNull Call<MevoData> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error has occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addPolygonLayer(@NonNull Style loadedMapStyle) {
        // Create and style a FillLayer that uses the Polygon Feature's coordinates in the GeoJSON data
        FillLayer countryPolygonFillLayer = new FillLayer("polygon", parkingSourceId);
        countryPolygonFillLayer.setProperties(
                PropertyFactory.fillColor(Color.RED),
                PropertyFactory.fillOpacity(.4f));
        countryPolygonFillLayer.setFilter(eq(literal("$type"), literal("Polygon")));
        loadedMapStyle.addLayer(countryPolygonFillLayer);
    }

    private void addPointsLayer(@NonNull Style loadedMapStyle) {
        // Create and style a CircleLayer that uses the Point Features' coordinates in the GeoJSON data
        CircleLayer individualCirclesLayer = new CircleLayer("points", vehicleSourceId);
        individualCirclesLayer.setProperties(
                PropertyFactory.circleColor(Color.YELLOW),
                PropertyFactory.circleRadius(3f));
        individualCirclesLayer.setFilter(eq(literal("$type"), literal("Point")));
        loadedMapStyle.addLayer(individualCirclesLayer);
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
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