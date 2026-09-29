package fr.charlesmassuard.mymovies_api.config;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fr.charlesmassuard.mymovies_api.exceptions.UserException;
import fr.charlesmassuard.mymovies_api.service.I18nService;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final I18nService i18n;

    public GlobalExceptionHandler(I18nService i18n) {
        this.i18n = i18n;
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<Map<String, String>> handleUserException(UserException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException() {
        return ResponseEntity.badRequest().body(Map.of("message", i18n.get("error.invalidStatus")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", i18n.get("error.unexpected")));
    }
}
