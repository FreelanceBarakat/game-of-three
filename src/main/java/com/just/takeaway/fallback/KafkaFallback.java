package com.just.takeaway.fallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.just.takeaway.adapter.GameClient;
import com.just.takeaway.controller.dto.AbstractGameDto;
import com.just.takeaway.controller.dto.GameDto;
import com.just.takeaway.controller.dto.GameResultsDto;
import com.just.takeaway.controller.dto.TurnDto;
import com.just.takeaway.exception.handler.ExceptionHandlerAdvice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * This is a fallback code when one of the player has not started yet ( service is down ) we publish the game details to kafka!
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaFallback implements GameClient {
    private String gameSubmissionTopic;
    private String turnsTopic;
    private String gameResultsTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExceptionHandlerAdvice exceptionHandlerAdvice;

    @Autowired
    public KafkaFallback(
        @Value("${fallback.topic.other-process-topic.game-submission-topic}") final String gameSubmissionTopic,
        @Value("${fallback.topic.other-process-topic.turn-topic}") final String turnsTopic,
        @Value("${fallback.topic.other-process-topic.game-results-topic}") final String gameResultsTopic,
        final KafkaTemplate<String, String> kafkaTemplate,
        final ExceptionHandlerAdvice exceptionHandlerAdvice
    ) {
        this.gameSubmissionTopic = gameSubmissionTopic;
        this.turnsTopic = turnsTopic;
        this.gameResultsTopic = gameResultsTopic;
        this.kafkaTemplate = kafkaTemplate;
        this.exceptionHandlerAdvice = exceptionHandlerAdvice;
    }

    @Override
    public void communicateTurn(final TurnDto turnDto) throws JsonProcessingException {
        sendMessage(turnDto, turnsTopic);
    }


    @Override
    public void reportOtherPlayerLoser(final GameResultsDto gameDto) throws JsonProcessingException {
        sendMessage(gameDto, gameResultsTopic);
    }

    @Override
    public void submitGame(final GameDto gameDto) throws JsonProcessingException {
        sendMessage(gameDto, gameSubmissionTopic);
    }

    private void sendMessage(AbstractGameDto gameDto, String topic) throws JsonProcessingException {
        kafkaTemplate.send(
            topic,
            gameDto.getGameId(),
            objectMapper.writeValueAsString(gameDto)
        ).handle((stringSendResult, throwable) -> {
                if (throwable != null) {
                    exceptionHandlerAdvice.handleGeneralException(throwable);
                    return null;
                } else {
                    return stringSendResult;
                }
            }
        );
    }
}
