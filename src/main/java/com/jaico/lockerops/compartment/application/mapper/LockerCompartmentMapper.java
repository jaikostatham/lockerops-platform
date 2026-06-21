package com.jaico.lockerops.compartment.application.mapper;

import com.jaico.lockerops.compartment.api.dto.request.CreateLockerCompartmentRequest;
import com.jaico.lockerops.compartment.api.dto.response.LockerCompartmentResponse;
import com.jaico.lockerops.compartment.api.dto.request.UpdateLockerCompartmentRequest;
import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import com.jaico.lockerops.station.domain.model.LockerStation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LockerCompartmentMapper {

    public LockerCompartment toEntity(
            CreateLockerCompartmentRequest request,
            LockerStation lockerStation
    ) {
        return new LockerCompartment(
                request.getCompartmentNumber(),
                request.getSize(),
                request.getStatus(),
                lockerStation
        );
    }

    public void updateEntityFromRequest(
            UpdateLockerCompartmentRequest request,
            LockerCompartment lockerCompartment
    ) {
        lockerCompartment.setCompartmentNumber(request.getCompartmentNumber());
        lockerCompartment.setSize(request.getSize());
        lockerCompartment.setStatus(request.getStatus());
    }

    public LockerCompartmentResponse toResponse(LockerCompartment lockerCompartment) {
        return new LockerCompartmentResponse(
                lockerCompartment.getId(),
                lockerCompartment.getCompartmentNumber(),
                lockerCompartment.getSize(),
                lockerCompartment.getStatus(),
                lockerCompartment.getLockerStation().getId()
        );
    }

    public List<LockerCompartmentResponse> toResponseList(List<LockerCompartment> lockerCompartments) {
        return lockerCompartments.stream()
                .map(this::toResponse)
                .toList();
    }
}