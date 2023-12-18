package com.just.takeaway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.just.takeaway.controller.dto.GameDto;
import com.just.takeaway.controller.dto.GameListDto;
import com.just.takeaway.controller.dto.GameResultsDto;
import com.just.takeaway.controller.dto.TurnDto;
import com.just.takeaway.controller.mapper.GameDtoMapper;
import com.just.takeaway.service.GameService;
import com.just.takeaway.service.dto.GameServiceDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.ACCEPTED;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GameController implements GameApi {
    private final GameService gameService;
    private final GameDtoMapper gameDtoMapper;

    // ----------------     user input    ----------------  //
    @RequestMapping(method = RequestMethod.POST, value = GAME_URL, consumes = "application/json")
    @ResponseStatus(ACCEPTED)
    void startGame(@RequestBody final GameDto gameDto) throws JsonProcessingException {
        log.info("Player created a game with id={}", gameDto.getGameId());
        final GameServiceDto gameServiceDto = gameDtoMapper.toGameServiceDto(gameDto);
        gameService.startGame(gameServiceDto);
    }

    @RequestMapping(method = RequestMethod.GET, value = GAME_URL, consumes = "application/json")
    GameListDto getGames() {
        final List<GameServiceDto> activeGames = gameService.getGames();
        return gameDtoMapper.toGameDtoList(activeGames);
    }

    @RequestMapping(method = RequestMethod.POST, value = UPDATE_NUMBER_URL, consumes = "application/json")
    GameResultsDto updateNumber(@Valid @RequestBody final GameDto gameDto) throws JsonProcessingException {
        final GameServiceDto gameServiceDto = gameDtoMapper.toGameServiceDto(gameDto);
        return gameService.updateNumber(gameServiceDto);
    }

    // ---------------- intra service communication   ----------------  //
    @ResponseStatus(ACCEPTED)
    @Override
    public void communicateTurn(@Valid final TurnDto turnDto) throws JsonProcessingException {
        final GameServiceDto gameServiceDto = gameDtoMapper.toGameServiceDto(turnDto);
        gameService.receiveOtherPlayerTurn(gameServiceDto);
    }

    @ResponseStatus(ACCEPTED)
    @Override
    public void reportOtherPlayerLoser(final GameResultsDto gameResultsDto) {
        gameService.reportLoser(gameResultsDto);
    }

    @ResponseStatus(ACCEPTED)
    @Override
    public void submitGame(final GameDto gameDto) {
        log.info("Receiving a request to submit a game");
        final GameServiceDto gameServiceDto = gameDtoMapper.toGameServiceDto(gameDto);
        gameService.handleSubmittedGame(gameServiceDto);
    }


}
