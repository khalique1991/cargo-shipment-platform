package com.flyqatar.cargo.adapter.in.rest;



import com.flyqatar.cargo.application.service.exception.DuplicateShipmentException;
import com.flyqatar.cargo.application.service.exception.ShipmentNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ProblemDetail handleNotFound(
            ShipmentNotFoundException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.NOT_FOUND
        );

        problem.setTitle("Shipment not found");
        problem.setDetail(exception.getMessage());

        return problem;
    }

    @ExceptionHandler(DuplicateShipmentException.class)
    public ProblemDetail handleDuplicate(
            DuplicateShipmentException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.CONFLICT
        );

        problem.setTitle("Duplicate shipment");
        problem.setDetail(exception.getMessage());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.BAD_REQUEST
        );

        problem.setTitle("Validation failed");
        problem.setDetail(
                "One or more request fields are invalid"
        );

        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(
            DataIntegrityViolationException exception
    ) {

        /*
         * A production implementation should inspect the
         * underlying SQL state and constraint name.
         * Do not translate every integrity violation into 409:
         * some indicate a programming error or invalid data.
         */
        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.CONFLICT
        );

        problem.setTitle("Data integrity conflict");
        problem.setDetail(
                "The request conflicts with an existing record "
                        + "or a database constraint"
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException exception
    ) {

        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.BAD_REQUEST
        );

        problem.setTitle("Invalid request");
        problem.setDetail(exception.getMessage());

        return problem;
    }
}