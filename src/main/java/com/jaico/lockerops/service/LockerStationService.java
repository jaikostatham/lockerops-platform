package com.jaico.lockerops.service;

import com.jaico.lockerops.dto.CreateLockerStationRequest;
import com.jaico.lockerops.dto.LockerStationResponse;
import com.jaico.lockerops.dto.UpdateLockerStationRequest;
import com.jaico.lockerops.exception.ResourceNotFoundException;
import com.jaico.lockerops.mapper.LockerStationMapper;
import com.jaico.lockerops.model.LockerStation;
import com.jaico.lockerops.repository.LockerStationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LockerStationService {

    private final LockerStationRepository lockerStationRepository;
    private final LockerStationMapper lockerStationMapper;

    public LockerStationService(
            LockerStationRepository lockerStationRepository,
            LockerStationMapper lockerStationMapper
    ) {
        this.lockerStationRepository = lockerStationRepository;
        this.lockerStationMapper = lockerStationMapper;
    }

    @Transactional(readOnly = true)
    public List<LockerStationResponse> getAllLockerStations() {
        List<LockerStation> lockerStations = lockerStationRepository.findAll();

        return lockerStationMapper.toResponseList(lockerStations);
    }

    @Transactional(readOnly = true)
    public LockerStationResponse getLockerStationById(Long id) {
        LockerStation lockerStation = findLockerStationOrThrow(id);

        return lockerStationMapper.toResponse(lockerStation);
    }

    @Transactional
    public LockerStationResponse createLockerStation(CreateLockerStationRequest request) {
        LockerStation lockerStation = lockerStationMapper.toEntity(request);

        LockerStation savedLockerStation = lockerStationRepository.save(lockerStation);

        return lockerStationMapper.toResponse(savedLockerStation);
    }

    @Transactional
    public LockerStationResponse updateLockerStation(
            Long id,
            UpdateLockerStationRequest request
    ) {
        LockerStation lockerStation = findLockerStationOrThrow(id);

        lockerStationMapper.updateEntityFromRequest(request, lockerStation);

        LockerStation updatedLockerStation = lockerStationRepository.save(lockerStation);

        return lockerStationMapper.toResponse(updatedLockerStation);
    }

    @Transactional
    public void deleteLockerStation(Long id) {
        LockerStation lockerStation = findLockerStationOrThrow(id);

        lockerStationRepository.delete(lockerStation);
    }

    private LockerStation findLockerStationOrThrow(Long id) {
        return lockerStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estación de lockers no encontrada con id: " + id
                ));
    }
}