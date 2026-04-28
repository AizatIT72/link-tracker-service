package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.ScrapperDto.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleChatNotFound(ChatNotFoundException ex) {
        log.atWarn().setCause(ex).log("Чат не найден");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildError("404", ex));
    }

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleChatAlreadyExists(ChatAlreadyExistsException ex) {
        log.atWarn().setCause(ex).log("Чат уже существует");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(buildError("409", ex));
    }

    @ExceptionHandler(LinkAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkAlreadyExists(LinkAlreadyExistsException ex) {
        log.atWarn().setCause(ex).log("Ссылка уже существует");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(buildError("409", ex));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkNotFound(LinkNotFoundException ex) {
        log.atWarn().setCause(ex).log("Ссылка не найдена");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildError("404", ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.atWarn().setCause(ex).log("Ошибка валидации запроса");
        return ResponseEntity.badRequest().body(buildError("400", ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.atError().setCause(ex).log("Неожиданная ошибка");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildError("500", ex));
    }

    private ApiErrorResponse buildError(String code, Exception ex) {
        return ApiErrorResponse.builder()
                .description(ex.getMessage())
                .code(code)
                .exceptionName(ex.getClass().getSimpleName())
                .exceptionMessage(ex.getMessage())
                .stacktrace(Arrays.stream(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.toList()))
                .build();
    }
}
