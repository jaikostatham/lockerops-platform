package com.jaico.lockerops.reservation.application.service;

import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentSize;
import org.springframework.stereotype.Service;

@Service
public class ReservationPricingService {

    public static final String CURRENCY = "EUR";

    public long calculateAmountMinor(LockerCompartmentSize size, int durationMinutes) {
        long billableHours = (durationMinutes + 59L) / 60L;

        return billableHours * hourlyRateMinor(size);
    }

    private long hourlyRateMinor(LockerCompartmentSize size) {
        return switch (size) {
            case SMALL -> 150L;
            case MEDIUM -> 200L;
            case LARGE -> 300L;
            case EXTRA_LARGE -> 450L;
        };
    }
}
