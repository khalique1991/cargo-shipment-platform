package com.flyqatar.cargo.application.service.exception;

public class ShipmentNotFoundException
        extends RuntimeException {

    public ShipmentNotFoundException(String message) {
        super(message);
    }
}