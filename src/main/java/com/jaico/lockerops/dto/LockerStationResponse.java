package com.jaico.lockerops.dto;

import com.jaico.lockerops.model.LockerStationStatus;

public class LockerStationResponse {

    private Long id;
    private String name;
    private String model;
    private String manufacturer;
    private LockerStationStatus status;
    private String location;
    private String imageUrl;

    public LockerStationResponse(
            Long id,
            String name,
            String model,
            String manufacturer,
            LockerStationStatus status,
            String location,
            String imageUrl
    ) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.manufacturer = manufacturer;
        this.status = status;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public LockerStationStatus getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}