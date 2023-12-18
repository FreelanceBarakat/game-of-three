package com.just.takeaway.fallback.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.just.takeaway.controller.GameController;
import com.just.takeaway.controller.dto.GameDto;
import com.just.takeaway.controller.dto.GameResultsDto;
import com.just.takeaway.controller.dto.TurnDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * when one of the players is down the other player sned messages to kafka
 * This controller handles the messages when the down player is up again
 */
@Component
@RequiredArgsConstructor
public class FailedMessagesHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameController gameController;

    @KafkaListener(topics = {"${fallback.topic.current-process-topic.game-submission-topic}"})
    public void listenMissedSubmittedGames(final String message) throws JsonProcessingException {
        final GameDto gameDto = objectMapper.readValue(message, GameDto.class);
        gameController.submitGame(gameDto);
    }

    @KafkaListener(topics = {"${fallback.topic.current-process-topic.turn-topic}"})
    public void listenToMissedTurns(final String message) throws JsonProcessingException {
        final TurnDto turnDto = objectMapper.readValue(message, TurnDto.class);
        gameController.communicateTurn(turnDto);
    }

    @KafkaListener(topics = {"${fallback.topic.current-process-topic.game-results-topic}"})
    public void listenToMissedGameResults(final String message) throws JsonProcessingException {
        final GameResultsDto gameResultsDto = objectMapper.readValue(message, GameResultsDto.class);
        gameController.reportOtherPlayerLoser(gameResultsDto);
    }


}
