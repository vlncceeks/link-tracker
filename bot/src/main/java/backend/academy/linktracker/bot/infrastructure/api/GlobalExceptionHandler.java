package backend.academy.linktracker.bot.infrastructure.api;

import backend.academy.linktracker.bot.application.dto.response.ApiErrorResponse;
import java.util.Arrays;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        "Некорректные параметры запроса",
                        "400",
                        e.getClass().getName(),
                        e.getMessage(),
                        Arrays.stream(e.getStackTrace())
                                .map(StackTraceElement::toString)
                                .toList()));
    }
}
