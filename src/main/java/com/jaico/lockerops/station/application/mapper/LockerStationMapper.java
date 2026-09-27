package com.jaico.lockerops.station.application.mapper;

import com.jaico.lockerops.station.api.dto.request.CreateLockerStationRequest;
import com.jaico.lockerops.station.api.dto.response.LockerStationResponse;
import com.jaico.lockerops.station.api.dto.request.UpdateLockerStationRequest;
import com.jaico.lockerops.station.domain.model.LockerStation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LockerStationMapper {

    public LockerStation toEntity(CreateLockerStationRequest request) {
        LockerStation lockerStation = new LockerStation();

        lockerStation.setName(request.getName());
        lockerStation.setModel(request.getModel());
        lockerStation.setManufacturer(request.getManufacturer());
        lockerStation.setStatus(request.getStatus());
        lockerStation.setLocation(request.getLocation());
        lockerStation.setImageUrl(request.getImageUrl());

        return lockerStation;
    }

    public void updateEntityFromRequest(
            UpdateLockerStationRequest request,
            LockerStation lockerStation
    ) {
        lockerStation.setName(request.getName());
        lockerStation.setModel(request.getModel());
        lockerStation.setManufacturer(request.getManufacturer());
        lockerStation.setStatus(request.getStatus());
        lockerStation.setLocation(request.getLocation());
        lockerStation.setImageUrl(request.getImageUrl());
    }

    public LockerStationResponse toResponse(LockerStation lockerStation) {
        return new LockerStationResponse(
                lockerStation.getId(),
                lockerStation.getName(),
                lockerStation.getModel(),
                lockerStation.getManufacturer(),
                lockerStation.getStatus(),
                lockerStation.getLocation(),
                lockerStation.getImageUrl()
        );
    }

    public List<LockerStationResponse> toResponseList(List<LockerStation> lockerStations) {
        return lockerStations.stream()
                .map(this::toResponse)
                .toList();
    }
}