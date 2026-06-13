package com.jaico.lockerops.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaico.lockerops.model.LockerStation;
import com.jaico.lockerops.service.LockerStationService;

@RestController
@RequestMapping("/api/locker-stations")
public class LockerStationController {

    private final LockerStationService lockerStationService;

    public LockerStationController(LockerStationService lockerStationService) {
        this.lockerStationService = lockerStationService;
    }

    @GetMapping
    public List<LockerStation> getAllLockerStations() {
        return lockerStationService.getAllLockerStations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LockerStation> getLockerStationById(@PathVariable Long id) {
        LockerStation lockerStation = lockerStationService.getLockerStationById(id);

        return ResponseEntity.ok(lockerStation);
    }

    @PostMapping
    public ResponseEntity<LockerStation> createLockerStation(
            @Valid @RequestBody LockerStation lockerStation) {

        LockerStation savedLockerStation = lockerStationService.createLockerStation(lockerStation);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedLockerStation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LockerStation> updateLockerStation(
            @PathVariable Long id,
            @Valid @RequestBody LockerStation lockerStationDetails) {

        LockerStation updatedLockerStation = lockerStationService.updateLockerStation(
                id,
                lockerStationDetails
        );

        return ResponseEntity.ok(updatedLockerStation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLockerStation(@PathVariable Long id) {
        lockerStationService.deleteLockerStation(id);

        return ResponseEntity.noContent().build();
    }
}
