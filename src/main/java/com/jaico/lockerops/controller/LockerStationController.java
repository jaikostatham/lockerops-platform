package com.jaico.lockerops.controller;

import com.jaico.lockerops.dto.CreateLockerStationRequest;
import com.jaico.lockerops.dto.LockerStationResponse;
import com.jaico.lockerops.dto.UpdateLockerStationRequest;
import com.jaico.lockerops.service.LockerStationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locker-stations")
public class LockerStationController {

    private final LockerStationService lockerStationService;

    public LockerStationController(LockerStationService lockerStationService) {
        this.lockerStationService = lockerStationService;
    }

    @GetMapping
    public ResponseEntity<List<LockerStationResponse>> getAllLockerStations() {
        List<LockerStationResponse> lockerStations = lockerStationService.getAllLockerStations();

        return ResponseEntity.ok(lockerStations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LockerStationResponse> getLockerStationById(@PathVariable Long id) {
        LockerStationResponse lockerStation = lockerStationService.getLockerStationById(id);

        return ResponseEntity.ok(lockerStation);
    }

    @PostMapping
    public ResponseEntity<LockerStationResponse> createLockerStation(
            @Valid @RequestBody CreateLockerStationRequest request) {

        LockerStationResponse createdLockerStation = lockerStationService.createLockerStation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdLockerStation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LockerStationResponse> updateLockerStation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLockerStationRequest request) {

        LockerStationResponse updatedLockerStation = lockerStationService.updateLockerStation(
                id,
                request
        );

        return ResponseEntity.ok(updatedLockerStation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLockerStation(@PathVariable Long id) {
        lockerStationService.deleteLockerStation(id);

        return ResponseEntity.noContent().build();
    }
}