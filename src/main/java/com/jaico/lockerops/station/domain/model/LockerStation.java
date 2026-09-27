package com.jaico.lockerops.station.domain.model;

import com.jaico.lockerops.station.domain.enums.LockerStationStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "locker_stations")
public class LockerStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false, length = 100)
    private String manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LockerStationStatus status;

    @Column(nullable = false, length = 150)
    private String location;

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
