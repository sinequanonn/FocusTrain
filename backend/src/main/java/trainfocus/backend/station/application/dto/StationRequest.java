package trainfocus.backend.station.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StationRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull BigDecimal latitude,
        @NotNull BigDecimal longitude
) {
}
