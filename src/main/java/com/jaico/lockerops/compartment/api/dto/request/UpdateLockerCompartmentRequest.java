package com.jaico.lockerops.compartment.api.dto.request;

import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentSize;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class UpdateLockerCompartmentRequest {

    @NotNull(message = "El número del compartimento es obligatorio")
    @Positive(message = "El número del compartimento debe ser mayor que cero")
    private Integer compartmentNumber;

    @NotNull(message = "El tamaño del compartimento es obligatorio")
    private LockerCompartmentSize size;

    @NotNull(message = "El estado del compartimento es obligatorio")
    private LockerCompartmentStatus status;

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
}