package com.just.takeaway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.just.takeaway.controller.dto.GameDto;
import com.just.takeaway.controller.dto.GameResultsDto;
import com.just.takeaway.controller.dto.TurnDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface GameApi {
    String GAME_URL = "/game";
    String TURN_URL = "/turn";
    String UPDATE_NUMBER_URL = "/update-number";
    String REPORT_GAME_RESULT_URL = "/report-game-result";
    String SUBMIT_GAME_URL = "/submit-game";

    @RequestMapping(method = RequestMethod.POST, value = TURN_URL, consumes = "application/json")
    void communicateTurn(@RequestBody final TurnDto turnDto) throws JsonProcessingException;


    @RequestMapping(method = RequestMethod.POST, value = REPORT_GAME_RESULT_URL, consumes = "application/json")
    void reportOtherPlayerLoser(@RequestBody final GameResultsDto gameDto) throws JsonProcessingException;

    @RequestMapping(method = RequestMethod.POST, value = SUBMIT_GAME_URL, consumes = "application/json")
    void submitGame(@RequestBody @Valid final GameDto gameDto) throws JsonProcessingException;
}
