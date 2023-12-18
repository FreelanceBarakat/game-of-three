package com.just.takeaway.exception.handler;

import com.just.takeaway.exception.NotYourTurnException;
import com.just.takeaway.exception.handler.dto.ErrorResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ExceptionHandlerAdviceTest {
    private static final String MESSAGE = "message";

    @InjectMocks
    private ExceptionHandlerAdvice exceptionHandlerAdvice;

    @Test
    void handleBadRequest_when_receiving__exception_should_handle_successfully() {
        Assertions.assertThat(
                exceptionHandlerAdvice.handleBadRequest(new NotYourTurnException(MESSAGE))
            )
            .isNotNull()
            .extracting(ErrorResponse::getErrorCode,
                ErrorResponse::getErrorMessage).containsExactly(
                HttpStatus.BAD_REQUEST.toString(),
                MESSAGE
            );
    }

    @Test
    void handleGeneralException_when_receiving__exception_should_handle_successfully() {
        Assertions.assertThat(
                exceptionHandlerAdvice.handleGeneralException(new Throwable(MESSAGE))
            )
            .isNotNull()
            .extracting(ErrorResponse::getErrorCode,
                ErrorResponse::getErrorMessage).containsExactly(
                HttpStatus.BAD_GATEWAY.toString(),
                MESSAGE
            );
    }
}
