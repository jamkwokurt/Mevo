package com.example.mevo.data;

import com.mapbox.geojson.Feature;

import java.util.List;

public class MevoData {
    String type;
    List<Feature> vehicleFeatures;
    Feature parkingFeature;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Feature> getVehicleFeatures() {
        return vehicleFeatures;
    }

    public void setVehicleFeatures(List<Feature> vehicleFeatures) {
        this.vehicleFeatures = vehicleFeatures;
    }

    public Feature getParkingFeature() {
        return parkingFeature;
    }

    public void setParkingFeature(Feature parkingFeature) {
        this.parkingFeature = parkingFeature;
    }
}
