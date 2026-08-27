package pe.edu.upeu.PharmaBackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursosNoEncontradosException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RecursosNoEncontradosException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), null);
    }
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(ReglaNegocioException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), null);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Los datos enviados no son válidos", validationErrors
        );
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception exception) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", null);
    }
    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, Map<String, String> validationErrors) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                validationErrors
        );
        return ResponseEntity.status(status).body(body);
    }
    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            Map<String, String> validationErrors) {
    }
}
