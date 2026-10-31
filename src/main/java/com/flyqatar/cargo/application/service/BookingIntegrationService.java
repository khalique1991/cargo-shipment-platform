package com.flyqatar.cargo.application.service;

import com.flyqatar.cargo.adapter.out.integration.booking.BookingServiceClient;
import org.springframework.stereotype.Service;

@Service
public class BookingIntegrationService {

    private final BookingServiceClient bookingServiceClient;

    public BookingIntegrationService(
            BookingServiceClient bookingServiceClient) {
        this.bookingServiceClient = bookingServiceClient;
    }

    public String checkBookingService() {
        return bookingServiceClient.checkBookingService();
    }
}