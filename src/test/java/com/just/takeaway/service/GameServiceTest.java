package com.just.takeaway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.just.takeaway.adapter.GameClient;
import com.just.takeaway.controller.dto.GameDto;
import com.just.takeaway.controller.dto.GameResultsDto;
import com.just.takeaway.controller.dto.TurnDto;
import com.just.takeaway.controller.mapper.GameDtoMapper;
import com.just.takeaway.exception.AutomaticPlayException;
import com.just.takeaway.exception.GameIsFinishedException;
import com.just.takeaway.exception.GameNotStartedException;
import com.just.takeaway.repository.GameRepository;
import com.just.takeaway.service.dto.GameServiceDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.just.takeaway.controller.dto.GameResultsDto.ResultDto.LOSER;
import static com.just.takeaway.controller.dto.GameResultsDto.ResultDto.WINNER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GameServiceTest {
    private static final String GAME_ID = "gameId";
    private static final long NUMBER = 10L;

    @InjectMocks
    private GameService gameService;
    @Mock
    private GameClient gameClient;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameDtoMapper gameDtoMapper;

    @Test
    void startGame_when_given_game_details_should_save_and_submit_game() throws JsonProcessingException {
        final GameServiceDto gameServiceDto = GameServiceDto.builder()
            .gameId(GAME_ID)
            .number(NUMBER)
            .isAutoPlay(false)
            .isFinished(false)
            .isWinner(false)
            .isGameStarted(false)
            .waitingForTurn(false)
            .build();

        final GameDto gameDto = GameDto.builder().build();
        when(gameDtoMapper.toGameDto(gameServiceDto)).thenReturn(gameDto);

        gameService.startGame(gameServiceDto);

        verify(gameClient).submitGame(any(GameDto.class));
        verify(gameRepository).save(gameServiceDto);
    }

    @Test
    void updateNumber_when_given_valid_game_with_auto_play_turned_on_should_throw_exception() throws JsonProcessingException {
        final GameServiceDto gameServiceDto = GameServiceDto.builder()
            .gameId(GAME_ID)
            .number(NUMBER)
            .isAutoPlay(true)
            .isFinished(false)
            .isWinner(false)
            .isGameStarted(true)
            .waitingForTurn(false)
            .build();

        when(gameRepository.findByIdOrThrow(GAME_ID)).thenReturn(gameServiceDto);

        Assertions.assertThatThrownBy(() -> gameService.updateNumber(gameServiceDto))
            .isInstanceOf(AutomaticPlayException.class);
    }

    @Test
    void updateNumber_when_game_is_finished_should_return_results() throws JsonProcessingException {
        final GameServiceDto gameServiceDto = GameServiceDto.builder()
            .gameId(GAME_ID)
            .number(NUMBER)
            .isAutoPlay(true)
            .isFinished(true)
            .isWinner(true)
            .isGameStarted(true)
            .waitingForTurn(false)
            .build();

        final GameResultsDto gameResults = GameResultsDto.builder()
            .result(WINNER)
            .gameId(GAME_ID)
            .build();

        when(gameRepository.findByIdOrThrow(GAME_ID)).thenReturn(gameServiceDto);

        Assertions.assertThatThrownBy(() -> gameService.updateNumber(gameServiceDto))
            .isInstanceOf(GameIsFinishedException.class);
    }

    @Test
    void updateNumber_when_game_running_should_return_update_numbers_and_communicate_turn() throws JsonProcessingException {
        final GameServiceDto gameServiceDto = GameServiceDto.builder()
            .gameId(GAME_ID)
            .number(NUMBER)
            .operation(GameServiceDto.Operation.ONE)
            .isAutoPlay(false)
            .isFinished(false)
            .isWinner(false)
            .isGameStarted(true)
            .waitingForTurn(false)
            .build();

        final TurnDto turnDto = TurnDto.builder()
            .gameId(GAME_ID)
            .build();

        when(gameRepository.findByIdOrThrow(GAME_ID)).thenReturn(gameServiceDto);
        when(gameDtoMapper.toTurnDto(gameServiceDto)).thenReturn(turnDto);

        gameService.updateNumber(gameServiceDto);

        Assertions.assertThat(gameServiceDto)
            .extracting(
                GameServiceDto::getNumber
            ).isEqualTo(3L);

        verify(gameRepository).updateSavedGame(gameServiceDto);
        verify(gameClient).communicateTurn(turnDto);
    }


    @Test
    void updateNumber_when_player_wins_should_update_inform_other_player() throws JsonProcessingException {
        final GameServiceDto gameServiceDto = GameServiceDto.builder()
            .gameId(GAME_ID)
            .number(2L)
            .operation(GameServiceDto.Operation.ONE)
            .isAutoPlay(false)
            .isFinished(false)
            .isWinner(false)
            .isGameStarted(true)
            .waitingForTurn(false)
            .build();

        final GameResultsDto gameResultsDto = GameResultsDto.builder()
            .gameId(GAME_ID)
            .result(LOSER)
            .build();

        when(gameRepository.findByIdOrThrow(GAME_ID)).thenReturn(gameServiceDto);
        when(gameDtoMapper.toLoserGameResultsDto(gameServiceDto)).thenReturn(gameResultsDto);

        gameService.updateNumber(gameServiceDto);

        Assertions.assertThat(gameServiceDto)
            .extracting(
                GameServiceDto::getNumber,
                GameServiceDto::isWinner,
                GameServiceDto::isFinished
            ).containsExactly(
                1L,
                true,
                true
            );

        verify(gameRepository).updateSavedGame(gameServiceDto);
        verify(gameClient).reportOtherPlayerLoser(gameResultsDto);
    }

    @Test
    void updateNumber_when_game_is_not_started_should_throw_exception() throws JsonProcessingException {
        final GameServiceDto gameServiceDto = GameServiceDto.builder()
            .gameId(GAME_ID)
            .number(NUMBER)
            .isAutoPlay(false)
            .isFinished(false)
            .isWinner(false)
            .isGameStarted(false)
            .waitingForTurn(false)
            .build();

        Assertions.assertThatThrownBy(() -> gameService.updateNumber(gameServiceDto))
            .isInstanceOf(GameNotStartedException.class);
    }

    @Test
    void handleSubmittedGame_when_receives_game_details_should_save_game_details() {
        final GameServiceDto gameServiceDto = GameServiceDto.builder()
            .gameId(GAME_ID)
            .number(NUMBER)
            .waitingForTurn(false)
            .isAutoPlay(false)
            .isFinished(false)
            .isWinner(false)
            .isGameStarted(false)
            .build();

        gameService.handleSubmittedGame(gameServiceDto);
        verify(gameRepository).save(gameServiceDto);
    }

    @Test
    void receiveOtherPlayerTurn_should_update_current_save_game_details() throws JsonProcessingException {
        final GameServiceDto gameServiceDto = GameServiceDto.builder()
            .gameId(GAME_ID)
            .number(2L)
            .operation(GameServiceDto.Operation.ONE)
            .waitingForTurn(false)
            .isAutoPlay(false)
            .isFinished(false)
            .isWinner(false)
            .isGameStarted(true)
            .build();

        when(gameRepository.findByIdOrThrow(anyString())).thenReturn(gameServiceDto);

        gameService.receiveOtherPlayerTurn(gameServiceDto);

        verify(gameRepository).updateSavedGame(gameServiceDto.getGameId(), gameServiceDto.getNumber(), true, false);
        verify(gameRepository).findByIdOrThrow(GAME_ID);
    }

    @Test
    void getGames_should_return_all_saved_games() {
        gameService.getGames();
        verify(gameRepository).getSavedGames();
    }

}
