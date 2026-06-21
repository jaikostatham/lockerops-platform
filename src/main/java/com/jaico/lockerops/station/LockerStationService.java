package com.jaico.lockerops.station;

import com.jaico.lockerops.station.CreateLockerStationRequest;
import com.jaico.lockerops.station.LockerStationResponse;
import com.jaico.lockerops.station.UpdateLockerStationRequest;
import com.jaico.lockerops.shared.exception.ApiErrorCode;
import com.jaico.lockerops.shared.exception.ApiException;
import com.jaico.lockerops.station.LockerStationMapper;
import com.jaico.lockerops.station.LockerStation;
import com.jaico.lockerops.station.LockerStationRepository;
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
                .orElseThrow(() -> new ApiException(
                        ApiErrorCode.LOCKER_STATION_NOT_FOUND,
                        id
                ));
    }
}
