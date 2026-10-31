package com.flyqatar.cargo.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateShipmentRequest(

        @NotBlank
        @Size(max = 32)
        String awbNumber,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        String originCode,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        String destinationCode,

        @Positive
        BigDecimal grossWeightKg,

        @Positive
        Integer pieceCount
) {
}