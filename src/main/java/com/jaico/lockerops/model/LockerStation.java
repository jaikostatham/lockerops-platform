package com.jaico.lockerops.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "locker_stations")
public class LockerStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la estación es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre de la estación debe tener entre 3 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede superar los 50 caracteres")
    @Column(nullable = false, length = 50)
    private String model;

    @NotBlank(message = "El fabricante es obligatorio")
    @Size(max = 100, message = "El fabricante no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String manufacturer;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LockerStationStatus status;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 150, message = "La ubicación no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String location;

    @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
