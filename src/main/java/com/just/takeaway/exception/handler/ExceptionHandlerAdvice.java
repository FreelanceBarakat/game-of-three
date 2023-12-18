package com.just.takeaway.exception.handler;


import com.just.takeaway.exception.GameNotStartedException;
import com.just.takeaway.exception.NotYourTurnException;
import com.just.takeaway.exception.handler.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerAdvice {

    @ExceptionHandler({
        NotYourTurnException.class,
        MethodArgumentNotValidException.class,
        IllegalArgumentException.class,
        GameNotStartedException.class}
    )
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(
        final Throwable exception
    ) {
        log.error(exception.getMessage(), exception);
        return ErrorResponse.builder()
            .errorCode(HttpStatus.BAD_REQUEST.toString())
            .errorMessage(exception.getMessage())
            .build();
    }


    @ExceptionHandler({Exception.class})
    @ResponseStatus(value = HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleGeneralException(
        final Throwable exception
    ) {
        log.error(exception.getMessage(), exception);
        return ErrorResponse.builder()
            .errorCode(HttpStatus.BAD_GATEWAY.toString())
            .errorMessage(exception.getMessage())
            .build();
    }

}
