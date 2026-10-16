package com.inn.paysense.exception;

import java.time.LocalDateTime;

public record ExceptionResponse(int status, String message, LocalDateTime dateTime) {
}
