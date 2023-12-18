package com.just.takeaway.repository;

import com.just.takeaway.exception.DuplicateGameIdException;
import com.just.takeaway.exception.EntityNotFoundException;
import com.just.takeaway.service.dto.GameServiceDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GameRepositoryTest {
    private static final String GAME_ID = "gameId";
    private static final long NUMBER = 10L;
    @InjectMocks
    private GameRepository gameRepository;

    @Test
    void save_when_game_does_not_exist_should_save_successfully() {
        Assertions.assertThat(
                gameRepository.save(
                    GameServiceDto.builder()
                        .gameId(GAME_ID)
                        .number(NUMBER)
                        .isAutoPlay(false)
                        .isFinished(false)
                        .isWinner(false)
                        .isGameStarted(false)
                        .waitingForTurn(false)
                        .build()))
            .isNotNull()
            .extracting(
                GameServiceDto::getNumber,
                GameServiceDto::getGameId
            ).containsExactly(
                NUMBER,
                GAME_ID
            );
    }


    @Test
    void save_when_game_already_exist_should_throw_exception() {
        gameRepository.save(
            GameServiceDto.builder()
                .gameId(GAME_ID)
                .number(NUMBER)
                .isAutoPlay(false)
                .isFinished(false)
                .isWinner(false)
                .isGameStarted(false)
                .waitingForTurn(false)
                .build());

        Assertions.assertThatThrownBy(() ->
                gameRepository.save(
                    GameServiceDto.builder()
                        .gameId(GAME_ID)
                        .number(NUMBER)
                        .isAutoPlay(false)
                        .isFinished(false)
                        .isWinner(false)
                        .isGameStarted(false)
                        .waitingForTurn(false)
                        .build()))
            .isInstanceOf(DuplicateGameIdException.class);
    }

    @Test
    void update_when_game_does_exist_should_save_successfully() {
        gameRepository.save(
            GameServiceDto.builder()
                .gameId(GAME_ID)
                .number(NUMBER)
                .isAutoPlay(false)
                .isFinished(false)
                .isWinner(false)
                .isGameStarted(false)
                .waitingForTurn(false)
                .build());

        Assertions.assertThat(
                gameRepository.updateSavedGame(
                    GameServiceDto.builder()
                        .gameId(GAME_ID)
                        .number(NUMBER + 1)
                        .isAutoPlay(true)
                        .isFinished(false)
                        .isWinner(false)
                        .isGameStarted(false)
                        .waitingForTurn(false)
                        .build()))
            .isNotNull()
            .extracting(
                GameServiceDto::getNumber,
                GameServiceDto::getGameId,
                GameServiceDto::isAutoPlay
            ).containsExactly(
                NUMBER + 1,
                GAME_ID,
                true
            );
    }

    @Test
    void update_when_game_does_not_exist_should_throw_exception() {
        Assertions.assertThatThrownBy(() ->
                gameRepository.updateSavedGame(
                    GameServiceDto.builder()
                        .gameId(GAME_ID)
                        .number(NUMBER)
                        .isAutoPlay(false)
                        .isFinished(false)
                        .isWinner(false)
                        .isGameStarted(false)
                        .waitingForTurn(false)
                        .build()))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getSavedGames_when_game_does_exist_should_return_all_games() {
        gameRepository.save(
            GameServiceDto.builder()
                .gameId(GAME_ID)
                .number(NUMBER)
                .isAutoPlay(false)
                .isFinished(false)
                .isWinner(false)
                .isGameStarted(false)
                .waitingForTurn(false)
                .build());

        Assertions.assertThat(
                gameRepository.getSavedGames()
            )
            .isNotNull()
            .hasSize(1);
    }
}
