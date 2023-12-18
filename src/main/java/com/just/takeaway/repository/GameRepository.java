package com.just.takeaway.repository;

import com.just.takeaway.exception.DuplicateGameIdException;
import com.just.takeaway.exception.EntityNotFoundException;
import com.just.takeaway.service.dto.GameServiceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class GameRepository {
    private final HashMap<String, GameServiceDto> gameList = new HashMap<>();

    public GameServiceDto save(final GameServiceDto gameServiceDto) {
        return gameList.compute(gameServiceDto.getGameId(), (gameId, savedGame) -> {
            if (savedGame != null) {
                throw new DuplicateGameIdException(String.format("Can't start game! game_id=%s already exists!", gameId));
            } else {
                log.info("Saving a new game={}", gameServiceDto);
                return gameServiceDto;
            }
        });
    }

    public GameServiceDto updateSavedGame(final GameServiceDto gameServiceDto) {
        log.info("Updating saved_game isAutoPlay={}, number={}, isFinished={}, isWinner={}", gameServiceDto.isAutoPlay(), gameServiceDto.getNumber(), gameServiceDto.isFinished(), gameServiceDto.isWinner());
        final GameServiceDto savedGame = findByIdOrThrow(gameServiceDto.getGameId());

        savedGame.setAutoPlay(gameServiceDto.isAutoPlay());
        savedGame.setNumber(gameServiceDto.getNumber());
        savedGame.setFinished(gameServiceDto.isFinished());
        savedGame.setWinner(gameServiceDto.isWinner());
        savedGame.setWaitingForTurn(true);
        return savedGame;
    }

    public GameServiceDto findByIdOrThrow(final String gameId) {
        return Optional.ofNullable(findById(gameId))
            .orElseThrow(() -> new EntityNotFoundException(String.format("Game_id=%s doesn't exist", gameId)));
    }

    public List<GameServiceDto> getSavedGames() {
        return gameList.values().stream().toList();
    }

    public void updateAutoPlay(String gameId, boolean isAutoPlay) {
        findByIdOrThrow(gameId).setAutoPlay(isAutoPlay);
    }

    public void updateSavedGame(String gameId, Long number, boolean isGameStarted, boolean waitingForTurn) {
        final GameServiceDto savedGame = findByIdOrThrow(gameId);
        savedGame.setNumber(number);
        savedGame.setGameStarted(isGameStarted);
        savedGame.setWaitingForTurn(waitingForTurn);
    }

    public void saveCurrentPlayerAsLoser(final String gameId) {
        final GameServiceDto savedGame = findByIdOrThrow(gameId);
        savedGame.setNumber(1L);
        savedGame.setFinished(true);
        savedGame.setWinner(false);
        savedGame.setWaitingForTurn(false);
    }

    private GameServiceDto findById(final String gameId) {
        return gameList.get(gameId);
    }
}
