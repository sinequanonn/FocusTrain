package trainfocus.backend.common.ui;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp,
        String path,
        List<FieldError> fieldErrors
) {
    public ErrorResponse(String errorCode, String message, LocalDateTime timestamp, String path) {
        this(errorCode, message, timestamp, path, null);
    }

    public record FieldError(String field, Object rejectedValue, String reason) {
    }
}
