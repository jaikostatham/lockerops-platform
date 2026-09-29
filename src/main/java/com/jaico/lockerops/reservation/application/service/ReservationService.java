package com.jaico.lockerops.reservation.application.service;

import com.jaico.lockerops.reservation.api.dto.request.CreateReservationRequest;
import com.jaico.lockerops.reservation.api.dto.response.ReservationResponse;
import com.jaico.lockerops.shared.exception.ApiErrorCode;
import com.jaico.lockerops.shared.exception.ApiException;
import com.jaico.lockerops.reservation.application.mapper.ReservationMapper;
import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentStatus;
import com.jaico.lockerops.reservation.domain.model.Reservation;
import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;
import com.jaico.lockerops.compartment.infrastructure.persistence.repository.LockerCompartmentRepository;
import com.jaico.lockerops.reservation.infrastructure.persistence.repository.ReservationRepository;
import com.jaico.lockerops.ticket.application.service.TicketService;
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
    private final ReservationPricingService reservationPricingService;
    private final ReservationPaymentProperties reservationPaymentProperties;
    private final TicketService ticketService;

    public ReservationService(
            ReservationRepository reservationRepository,
            LockerCompartmentRepository lockerCompartmentRepository,
            ReservationMapper reservationMapper,
            ReservationPricingService reservationPricingService,
            ReservationPaymentProperties reservationPaymentProperties,
            TicketService ticketService
    ) {
        this.reservationRepository = reservationRepository;
        this.lockerCompartmentRepository = lockerCompartmentRepository;
        this.reservationMapper = reservationMapper;
        this.reservationPricingService = reservationPricingService;
        this.reservationPaymentProperties = reservationPaymentProperties;
        this.ticketService = ticketService;
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
        Instant paymentExpiresAt = reservedFrom.plus(
                reservationPaymentProperties.getTimeoutMinutes(),
                ChronoUnit.MINUTES
        );
        long amountMinor = reservationPricingService.calculateAmountMinor(
                lockerCompartment.getSize(),
                request.getDurationMinutes()
        );

        Reservation reservation = reservationMapper.toEntity(
                lockerCompartment,
                reservedFrom,
                reservedUntil,
                request.getCustomerReference(),
                amountMinor,
                ReservationPricingService.CURRENCY,
                paymentExpiresAt
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
        Reservation reservation = findReservationForUpdateOrThrow(id);

        validateReservationCanBeCancelled(reservation);

        Instant now = Instant.now();

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(now);

        LockerCompartment lockerCompartment = reservation.getLockerCompartment();
        lockerCompartment.setStatus(LockerCompartmentStatus.AVAILABLE);
        ticketService.cancelTicketForReservation(reservation, now);

        Reservation cancelledReservation = reservationRepository.save(reservation);

        return reservationMapper.toResponse(cancelledReservation);
    }

    private LockerCompartment findLockerCompartmentOrThrow(Long id) {
        return lockerCompartmentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(
                        ApiErrorCode.LOCKER_COMPARTMENT_NOT_FOUND,
                        id
                ));
    }

    private Reservation findReservationOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        ApiErrorCode.RESERVATION_NOT_FOUND,
                        id
                ));
    }

    private Reservation findReservationForUpdateOrThrow(Long id) {
        return reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(
                        ApiErrorCode.RESERVATION_NOT_FOUND,
                        id
                ));
    }

    private void validateLockerCompartmentIsAvailable(LockerCompartment lockerCompartment) {
        if (lockerCompartment.getStatus() != LockerCompartmentStatus.AVAILABLE) {
            throw new ApiException(
                    ApiErrorCode.LOCKER_COMPARTMENT_NOT_AVAILABLE
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
            throw new ApiException(
                    ApiErrorCode.LOCKER_COMPARTMENT_ACTIVE_RESERVATION
            );
        }
    }

    private void validateReservationCanBeCancelled(Reservation reservation) {
        if (!ACTIVE_RESERVATION_STATUSES.contains(reservation.getStatus())) {
            throw new ApiException(
                    ApiErrorCode.RESERVATION_CANNOT_BE_CANCELLED
            );
        }
    }
}
