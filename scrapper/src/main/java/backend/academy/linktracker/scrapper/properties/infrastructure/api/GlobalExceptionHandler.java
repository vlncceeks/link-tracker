package backend.academy.linktracker.scrapper.properties.infrastructure.api;

import backend.academy.linktracker.scrapper.properties.application.dto.response.ApiErrorResponse;
import backend.academy.linktracker.scrapper.properties.application.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.properties.application.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.properties.application.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.properties.application.exception.LinkNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleChatNotFound(ChatNotFoundException e) {
        return ResponseEntity.status(404).body(error("404", e));
    }

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleChatExists(ChatAlreadyExistsException e) {
        return ResponseEntity.status(409).body(error("409", e));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkNotFound(LinkNotFoundException e) {
        return ResponseEntity.status(404).body(error("404", e));
    }

    @ExceptionHandler(LinkAlreadyTrackedException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkExists(LinkAlreadyTrackedException e) {
        return ResponseEntity.status(409).body(error("409", e));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(error("400", e));
    }

    private ApiErrorResponse error(String code, Exception e) {
        return new ApiErrorResponse(
            e.getMessage(), code,
            e.getClass().getName(), e.getMessage(),
            Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList()
        );
    }
}
