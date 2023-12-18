package com.just.takeaway.adapter.fallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.just.takeaway.controller.dto.GameDto;
import com.just.takeaway.controller.dto.GameResultsDto;
import com.just.takeaway.controller.dto.TurnDto;
import com.just.takeaway.exception.handler.ExceptionHandlerAdvice;
import com.just.takeaway.fallback.KafkaFallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class KafkaFallbackTest {
    public static final String GAME_ID = "game_id";
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    final String gameSubmissionTopic = "gameSubmissionTopic";
    final String turnsTopic = "turnsTopic";
    final String gameResultsTopic = "gameResultsTopic";

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private ExceptionHandlerAdvice exceptionHandlerAdvice;
    private KafkaFallback kafkaFallback;

    @BeforeEach
    void init() {
        kafkaFallback = new KafkaFallback(
            gameSubmissionTopic,
            turnsTopic,
            gameResultsTopic,
            kafkaTemplate,
            exceptionHandlerAdvice
        );
    }

    @Test
    void communicateTurn_when_given_turn_to_send_should_send_successfully() throws JsonProcessingException {
        mockKafkaTemplate();

        final TurnDto turn = TurnDto.builder().gameId(GAME_ID).build();

        kafkaFallback.communicateTurn(turn);
        verify(kafkaTemplate).send(turnsTopic, GAME_ID, objectMapper.writeValueAsString(turn));
    }

    @Test
    void reportOtherPlayerLoser_when_given_game_result_to_send_should_send_successfully() throws JsonProcessingException {
        mockKafkaTemplate();

        final GameResultsDto gameResultsDto = GameResultsDto.builder().gameId(GAME_ID).build();

        kafkaFallback.reportOtherPlayerLoser(gameResultsDto);
        verify(kafkaTemplate).send(gameResultsTopic, GAME_ID, objectMapper.writeValueAsString(gameResultsDto));
    }

    @Test
    void submitGame_when_given_game_details_to_send_should_send_successfully() throws JsonProcessingException {
        mockKafkaTemplate();

        final GameDto gameDto = GameDto.builder().gameId(GAME_ID).build();

        kafkaFallback.submitGame(gameDto);
        verify(kafkaTemplate).send(gameSubmissionTopic, GAME_ID, objectMapper.writeValueAsString(gameDto));
    }

    private void mockKafkaTemplate() {
        when(kafkaTemplate.send(
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(CompletableFuture.completedFuture((mock(SendResult.class))));
    }

}
