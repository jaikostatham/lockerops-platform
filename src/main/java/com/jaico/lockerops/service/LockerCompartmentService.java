package com.jaico.lockerops.service;

import com.jaico.lockerops.dto.CreateLockerCompartmentRequest;
import com.jaico.lockerops.dto.LockerCompartmentResponse;
import com.jaico.lockerops.dto.UpdateLockerCompartmentRequest;
import com.jaico.lockerops.exception.ApiErrorCode;
import com.jaico.lockerops.exception.ApiException;
import com.jaico.lockerops.mapper.LockerCompartmentMapper;
import com.jaico.lockerops.model.LockerCompartment;
import com.jaico.lockerops.model.LockerStation;
import com.jaico.lockerops.repository.LockerCompartmentRepository;
import com.jaico.lockerops.repository.LockerStationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LockerCompartmentService {

    private final LockerCompartmentRepository lockerCompartmentRepository;
    private final LockerStationRepository lockerStationRepository;
    private final LockerCompartmentMapper lockerCompartmentMapper;

    public LockerCompartmentService(
            LockerCompartmentRepository lockerCompartmentRepository,
            LockerStationRepository lockerStationRepository,
            LockerCompartmentMapper lockerCompartmentMapper
    ) {
        this.lockerCompartmentRepository = lockerCompartmentRepository;
        this.lockerStationRepository = lockerStationRepository;
        this.lockerCompartmentMapper = lockerCompartmentMapper;
    }

    @Transactional
    public LockerCompartmentResponse createLockerCompartment(
            Long lockerStationId,
            CreateLockerCompartmentRequest request
    ) {
        LockerStation lockerStation = findLockerStationOrThrow(lockerStationId);

        validateCompartmentNumberIsNotDuplicated(
                lockerStationId,
                request.getCompartmentNumber()
        );

        LockerCompartment lockerCompartment = lockerCompartmentMapper.toEntity(
                request,
                lockerStation
        );

        LockerCompartment savedLockerCompartment = lockerCompartmentRepository.save(lockerCompartment);

        return lockerCompartmentMapper.toResponse(savedLockerCompartment);
    }

    @Transactional(readOnly = true)
    public List<LockerCompartmentResponse> getLockerCompartmentsByStationId(Long lockerStationId) {
        findLockerStationOrThrow(lockerStationId);

        List<LockerCompartment> lockerCompartments =
                lockerCompartmentRepository.findByLockerStation_Id(lockerStationId);

        return lockerCompartmentMapper.toResponseList(lockerCompartments);
    }

    @Transactional(readOnly = true)
    public LockerCompartmentResponse getLockerCompartmentById(Long id) {
        LockerCompartment lockerCompartment = findLockerCompartmentOrThrow(id);

        return lockerCompartmentMapper.toResponse(lockerCompartment);
    }

    @Transactional
    public LockerCompartmentResponse updateLockerCompartment(
            Long id,
            UpdateLockerCompartmentRequest request
    ) {
        LockerCompartment lockerCompartment = findLockerCompartmentOrThrow(id);

        Long lockerStationId = lockerCompartment.getLockerStation().getId();

        boolean compartmentNumberChanged = !lockerCompartment.getCompartmentNumber()
                .equals(request.getCompartmentNumber());

        if (compartmentNumberChanged) {
            validateCompartmentNumberIsNotDuplicated(
                    lockerStationId,
                    request.getCompartmentNumber()
            );
        }

        lockerCompartmentMapper.updateEntityFromRequest(request, lockerCompartment);

        LockerCompartment updatedLockerCompartment = lockerCompartmentRepository.save(lockerCompartment);

        return lockerCompartmentMapper.toResponse(updatedLockerCompartment);
    }

    @Transactional
    public void deleteLockerCompartment(Long id) {
        LockerCompartment lockerCompartment = findLockerCompartmentOrThrow(id);

        lockerCompartmentRepository.delete(lockerCompartment);
    }

    private LockerStation findLockerStationOrThrow(Long id) {
        return lockerStationRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        ApiErrorCode.LOCKER_STATION_NOT_FOUND,
                        id
                ));
    }

    private LockerCompartment findLockerCompartmentOrThrow(Long id) {
        return lockerCompartmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        ApiErrorCode.LOCKER_COMPARTMENT_NOT_FOUND,
                        id
                ));
    }

    private void validateCompartmentNumberIsNotDuplicated(
            Long lockerStationId,
            Integer compartmentNumber
    ) {
        boolean exists = lockerCompartmentRepository
                .existsByLockerStation_IdAndCompartmentNumber(lockerStationId, compartmentNumber);

        if (exists) {
            throw new ApiException(
                    ApiErrorCode.LOCKER_COMPARTMENT_DUPLICATED_NUMBER
            );
        }
    }
}
