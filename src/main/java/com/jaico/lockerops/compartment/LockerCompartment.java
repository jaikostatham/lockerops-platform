package com.jaico.lockerops.compartment;

import com.jaico.lockerops.station.LockerStation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "locker_compartments")
public class LockerCompartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compartment_number", nullable = false)
    private Integer compartmentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LockerCompartmentSize size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LockerCompartmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_station_id", nullable = false)
    private LockerStation lockerStation;

    public LockerCompartment() {
    }

    public LockerCompartment(
            Integer compartmentNumber,
            LockerCompartmentSize size,
            LockerCompartmentStatus status,
            LockerStation lockerStation
    ) {
        this.compartmentNumber = compartmentNumber;
        this.size = size;
        this.status = status;
        this.lockerStation = lockerStation;
    }

    public Long getId() {
        return id;
    }

    public Integer getCompartmentNumber() {
        return compartmentNumber;
    }

    public void setCompartmentNumber(Integer compartmentNumber) {
        this.compartmentNumber = compartmentNumber;
    }

    public LockerCompartmentSize getSize() {
        return size;
    }

    public void setSize(LockerCompartmentSize size) {
        this.size = size;
    }

    public LockerCompartmentStatus getStatus() {
        return status;
    }

    public void setStatus(LockerCompartmentStatus status) {
        this.status = status;
    }

    public LockerStation getLockerStation() {
        return lockerStation;
    }

    public void setLockerStation(LockerStation lockerStation) {
        this.lockerStation = lockerStation;
    }
}
