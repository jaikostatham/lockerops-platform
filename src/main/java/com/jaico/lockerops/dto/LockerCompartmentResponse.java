package com.jaico.lockerops.dto;

import com.jaico.lockerops.model.LockerCompartmentSize;
import com.jaico.lockerops.model.LockerCompartmentStatus;

public class LockerCompartmentResponse {

    private Long id;
    private Integer compartmentNumber;
    private LockerCompartmentSize size;
    private LockerCompartmentStatus status;
    private Long lockerStationId;

    public LockerCompartmentResponse(
            Long id,
            Integer compartmentNumber,
            LockerCompartmentSize size,
            LockerCompartmentStatus status,
            Long lockerStationId
    ) {
        this.id = id;
        this.compartmentNumber = compartmentNumber;
        this.size = size;
        this.status = status;
        this.lockerStationId = lockerStationId;
    }

    public Long getId() {
        return id;
    }

    public Integer getCompartmentNumber() {
        return compartmentNumber;
    }

    public LockerCompartmentSize getSize() {
        return size;
    }

    public LockerCompartmentStatus getStatus() {
        return status;
    }

    public Long getLockerStationId() {
        return lockerStationId;
    }
}