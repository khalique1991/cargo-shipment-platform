package com.flyqatar.cargo.application.service.exception;

public class DuplicateShipmentException
        extends RuntimeException {

    public DuplicateShipmentException(String message) {
        super(message);
    }
}