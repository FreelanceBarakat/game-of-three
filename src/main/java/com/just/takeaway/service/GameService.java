package com.just.takeaway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.just.takeaway.adapter.GameClient;
import com.just.takeaway.controller.dto.GameResultsDto;
import com.just.takeaway.controller.mapper.GameDtoMapper;
import com.just.takeaway.exception.AutomaticPlayException;
import com.just.takeaway.exception.GameIsFinishedException;
import com.just.takeaway.exception.GameNotStartedException;
import com.just.takeaway.exception.NotYourTurnException;
import com.just.takeaway.repository.GameRepository;
import com.just.takeaway.service.dto.GameServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * this service handles flow of requests coming from the controller.
 * Contains some validations as well
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameClient gameClient;
    private final GameRepository gameRepository;
    private final GameDtoMapper gameDtoMapper;

    public void handleSubmittedGame(final GameServiceDto gameServiceDto) {
        gameServiceDto.setGameStarted(true);
        gameServiceDto.setAutoPlay(false);
        gameRepository.save(gameServiceDto);
    }

    public void startGame(final GameServiceDto gameServiceDto) throws JsonProcessingException {
        gameRepository.save(gameServiceDto);
        gameClient.submitGame(gameDtoMapper.toGameDto(gameServiceDto));

    }

    public List<GameServiceDto> getGames() {
        return gameRepository.getSavedGames();
    }

    public GameResultsDto updateNumber(final GameServiceDto gameServiceDto) throws JsonProcessingException {
        if (!isGameStarted(gameServiceDto)) {
            throw new GameNotStartedException("You have to wait for other play to start game first!");
        }

        if (isWaitingForTurn(gameServiceDto)) {
            throw new NotYourTurnException("You have to wait for other play to take turn!");
        }

        if (isGameFinished(gameServiceDto)) {
            log.info("game_id={} is already finished! , are you a winner? => {}", gameServiceDto.getGameId(),
                gameRepository.findByIdOrThrow(gameServiceDto.getGameId()).isWinner());
            throw new GameIsFinishedException(
                String.format("game_id=%s is already finished! , are you a winner? => %s",
                    gameServiceDto.getGameId(),
                    gameRepository.findByIdOrThrow(gameServiceDto.getGameId()).isWinner())
            );
        }

        if (isAutoPlay(gameServiceDto)) {
            throw new AutomaticPlayException("You can't interfere in an automatic play!");
        }

        if (gameServiceDto.isAutoPlay()) { // game wasn't autoplay then turned into autoplay
            updateIsAutoPlay(gameServiceDto);
        }

        final GameServiceDto gameAfterTurn = takeTurn(gameServiceDto);
        return gameDtoMapper.toGameResultsDto(gameAfterTurn);
    }

    public void receiveOtherPlayerTurn(final GameServiceDto gameServiceDto) throws JsonProcessingException {
        log.info("Updating game_id={} number to {}", gameServiceDto.getGameId(), gameServiceDto.getNumber());
        gameRepository.updateSavedGame(gameServiceDto.getGameId(), gameServiceDto.getNumber(), true, false);

        if (isAutoPlay(gameServiceDto)) {
            log.info("Play is taking turn automatically!");
            takeTurn(gameRepository.findByIdOrThrow(gameServiceDto.getGameId()));
        }

    }

    public void reportLoser(final GameResultsDto gameResultsDto) {
        log.info("Reporting other player as a loser at game_id={}", gameResultsDto.getGameId());
        gameRepository.saveCurrentPlayerAsLoser(gameResultsDto.getGameId());
    }



    private GameServiceDto takeTurn(GameServiceDto gameServiceDto) throws JsonProcessingException {
        final GameServiceDto savedGame = gameRepository.findByIdOrThrow(gameServiceDto.getGameId());
        gameServiceDto.setNumber(savedGame.getNumber());

        final GameServiceDto gameAfterTurn = gameServiceDto.updateNumber();

        if (gameAfterTurn.isFinished() && gameAfterTurn.isWinner()) {
            gameRepository.updateSavedGame(gameAfterTurn);
            gameClient.reportOtherPlayerLoser(gameDtoMapper.toLoserGameResultsDto(gameAfterTurn));

        } else if (!gameAfterTurn.isFinished()) {
            gameRepository.updateSavedGame(gameAfterTurn);
            gameClient.communicateTurn(gameDtoMapper.toTurnDto(gameAfterTurn));
        }
        return gameAfterTurn;
    }

    private boolean isAutoPlay(final GameServiceDto gameServiceDto) {
        final GameServiceDto savedGame = gameRepository.findByIdOrThrow(gameServiceDto.getGameId());
        return savedGame.isAutoPlay();
    }

    private boolean isGameFinished(final GameServiceDto gameServiceDto) {
        final GameServiceDto savedGameServiceDto = gameRepository.findByIdOrThrow(gameServiceDto.getGameId());
        return savedGameServiceDto != null && savedGameServiceDto.isFinished();
    }


    private boolean isGameStarted(GameServiceDto gameServiceDto) {
        final GameServiceDto savedGame = gameRepository.findByIdOrThrow(gameServiceDto.getGameId());
        return savedGame != null && savedGame.isGameStarted();
    }


    private void updateIsAutoPlay(GameServiceDto gameServiceDto) {
        gameRepository.updateAutoPlay(gameServiceDto.getGameId(), true);

    }

    private boolean isWaitingForTurn(GameServiceDto gameServiceDto) {
        return gameRepository.findByIdOrThrow(gameServiceDto.getGameId()).isWaitingForTurn();
    }
}
