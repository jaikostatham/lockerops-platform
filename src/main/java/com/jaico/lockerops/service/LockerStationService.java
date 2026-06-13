package com.jaico.lockerops.service;

import java.util.List;
import java.util.Optional;

import com.jaico.lockerops.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import com.jaico.lockerops.model.LockerStation;
import com.jaico.lockerops.repository.LockerStationRepository;

@Service
public class LockerStationService {

    private final LockerStationRepository lockerStationRepository;

    public LockerStationService(LockerStationRepository lockerStationRepository) {
        this.lockerStationRepository = lockerStationRepository;
    }

    public List<LockerStation> getAllLockerStations() {
        return lockerStationRepository.findAll();
    }

    public LockerStation getLockerStationById(Long id) {
        return lockerStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Locker station not found with id: " + id
                ));
    }

    public LockerStation createLockerStation(LockerStation lockerStation) {
        return lockerStationRepository.save(lockerStation);
    }

    public LockerStation updateLockerStation(Long id, LockerStation lockerStationDetails) {
        LockerStation lockerStation = lockerStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Locker station not found with id: " + id
                ));

        lockerStation.setName(lockerStationDetails.getName());
        lockerStation.setModel(lockerStationDetails.getModel());
        lockerStation.setManufacturer(lockerStationDetails.getManufacturer());
        lockerStation.setStatus(lockerStationDetails.getStatus());
        lockerStation.setLocation(lockerStationDetails.getLocation());
        lockerStation.setImageUrl(lockerStationDetails.getImageUrl());

        return lockerStationRepository.save(lockerStation);
    }

    public void deleteLockerStation(Long id) {
        if (!lockerStationRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Locker station not found with id: " + id
            );
        }

        lockerStationRepository.deleteById(id);
    }
}
