package com.jaico.lockerops.service;

import com.jaico.lockerops.dto.CreateReservationRequest;
import com.jaico.lockerops.dto.ReservationResponse;
import com.jaico.lockerops.exception.ConflictException;
import com.jaico.lockerops.exception.ResourceNotFoundException;
import com.jaico.lockerops.mapper.ReservationMapper;
import com.jaico.lockerops.model.LockerCompartment;
import com.jaico.lockerops.model.LockerCompartmentStatus;
import com.jaico.lockerops.model.Reservation;
import com.jaico.lockerops.model.ReservationStatus;
import com.jaico.lockerops.repository.LockerCompartmentRepository;
import com.jaico.lockerops.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationService {

    private static final List<ReservationStatus> ACTIVE_RESERVATION_STATUSES = List.of(
            ReservationStatus.PENDING_PAYMENT,
            ReservationStatus.CONFIRMED
    );

    private final ReservationRepository reservationRepository;
    private final LockerCompartmentRepository lockerCompartmentRepository;
    private final ReservationMapper reservationMapper;

    public ReservationService(
            ReservationRepository reservationRepository,
            LockerCompartmentRepository lockerCompartmentRepository,
            ReservationMapper reservationMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.lockerCompartmentRepository = lockerCompartmentRepository;
        this.reservationMapper = reservationMapper;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        LockerCompartment lockerCompartment = findLockerCompartmentOrThrow(
                request.getLockerCompartmentId()
        );

        validateLockerCompartmentIsAvailable(lockerCompartment);

        validateLockerCompartmentDoesNotHaveActiveReservation(
                lockerCompartment.getId()
        );

        Instant reservedFrom = Instant.now();
        Instant reservedUntil = reservedFrom.plus(
                request.getDurationMinutes(),
                ChronoUnit.MINUTES
        );

        Reservation reservation = reservationMapper.toEntity(
                lockerCompartment,
                reservedFrom,
                reservedUntil,
                request.getCustomerReference()
        );

        lockerCompartment.setStatus(LockerCompartmentStatus.RESERVED);

        Reservation savedReservation = reservationRepository.save(reservation);

        return reservationMapper.toResponse(savedReservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservationMapper.toResponseList(reservations);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = findReservationOrThrow(id);

        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = findReservationOrThrow(id);

        validateReservationCanBeCancelled(reservation);

        Instant now = Instant.now();

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(now);

        LockerCompartment lockerCompartment = reservation.getLockerCompartment();
        lockerCompartment.setStatus(LockerCompartmentStatus.AVAILABLE);

        Reservation cancelledReservation = reservationRepository.save(reservation);

        return reservationMapper.toResponse(cancelledReservation);
    }

    private LockerCompartment findLockerCompartmentOrThrow(Long id) {
        return lockerCompartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compartimento no encontrado con id: " + id
                ));
    }

    private Reservation findReservationOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reserva no encontrada con id: " + id
                ));
    }

    private void validateLockerCompartmentIsAvailable(LockerCompartment lockerCompartment) {
        if (lockerCompartment.getStatus() != LockerCompartmentStatus.AVAILABLE) {
            throw new ConflictException(
                    "El compartimento no está disponible para reservar"
            );
        }
    }

    private void validateLockerCompartmentDoesNotHaveActiveReservation(Long lockerCompartmentId) {
        boolean hasActiveReservation = reservationRepository
                .existsByLockerCompartment_IdAndStatusIn(
                        lockerCompartmentId,
                        ACTIVE_RESERVATION_STATUSES
                );

        if (hasActiveReservation) {
            throw new ConflictException(
                    "El compartimento ya tiene una reserva activa"
            );
        }
    }

    private void validateReservationCanBeCancelled(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ConflictException(
                    "La reserva no se puede cancelar porque no está activa"
            );
        }
    }
}