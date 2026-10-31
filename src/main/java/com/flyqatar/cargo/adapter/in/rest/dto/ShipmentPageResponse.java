package com.flyqatar.cargo.adapter.in.rest.dto;

import java.util.List;

public record ShipmentPageResponse<T>(
        List<T> content,
        int page,
        int size
) {
}