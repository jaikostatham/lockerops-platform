package com.jaico.lockerops.station;

import com.jaico.lockerops.station.LockerStationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateLockerStationRequest {

    @NotBlank(message = "El nombre de la estación es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre de la estación debe tener entre 3 y 100 caracteres")
    private String name;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede superar los 50 caracteres")
    private String model;

    @NotBlank(message = "El fabricante es obligatorio")
    @Size(max = 100, message = "El fabricante no puede superar los 100 caracteres")
    private String manufacturer;

    @NotNull(message = "El estado es obligatorio")
    private LockerStationStatus status;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 150, message = "La ubicación no puede superar los 150 caracteres")
    private String location;

    @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres")
    private String imageUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LockerStationStatus getStatus() {
        return status;
    }

    public void setStatus(LockerStationStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}