package com.just.takeaway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.just.takeaway.config.SpringIntegrationTest;
import com.just.takeaway.controller.dto.GameDto;
import com.just.takeaway.controller.dto.GameResultsDto;
import com.just.takeaway.controller.dto.TurnDto;
import com.just.takeaway.repository.GameRepository;
import com.just.takeaway.service.dto.GameServiceDto;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.just.takeaway.controller.GameApi.GAME_URL;
import static com.just.takeaway.controller.GameApi.REPORT_GAME_RESULT_URL;
import static com.just.takeaway.controller.GameApi.SUBMIT_GAME_URL;
import static com.just.takeaway.controller.GameApi.TURN_URL;
import static com.just.takeaway.controller.GameApi.UPDATE_NUMBER_URL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@CucumberContextConfiguration
@AutoConfigureWireMock(port = 9999)
public class GameControllerStepDefinition extends SpringIntegrationTest {

    @SpyBean
    private GameRepository gameRepository;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ResultActions latestHttpCall;
    private Long number;
    private String gameId;

    @Given("^Player (\\d+) is mocked$")
    public void otherPlayerIsMocked(final int playerNo) {
        stubOtherPlayerForReceivingGameResults();
        stubOtherPlayerForReceivingTurn();
        stubOtherPlayerForReceivingSubmitGame();
    }

    @When("^player 1 starts the game with id (\\d+) and number (\\d+) and autoPlay \"([^\"]*)\"$")
    public void the_user_starts_the_game(final String gameId, final Long number, final boolean isAutoplay) throws Throwable {
        this.number = number;
        this.gameId = gameId;

        final GameDto gameDto = GameDto.builder()
            .gameId(gameId)
            .number(number)
            .autoPlay(isAutoplay)
            .build();

        latestHttpCall = mockMvc.perform(post(GAME_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(gameDto)));
    }

    @Then("^player 1 receives status code of (\\d+)$")
    public void user_gets_a_response(int statusCode) throws Throwable {
        latestHttpCall.andExpect(status().isAccepted());
    }

    @And("^the game data is saved locally$")
    public void the_game_data_is_saved() throws Throwable {
        Assertions.assertThat(gameRepository.findByIdOrThrow(gameId))
            .isNotNull()
            .extracting(GameServiceDto::getNumber)
            .isEqualTo(number);
    }


    @When("^player (\\d+) update the number to (\\d+) and send turn to player (\\d+)$")
    public void other_player_play_turn(final Long player1No, final Long number, final Long player2No) throws Throwable {
        this.number = number;

        final TurnDto gameDto = TurnDto.builder()
            .gameId(gameId)
            .number(number)
            .build();

        latestHttpCall = mockMvc.perform(post(TURN_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(gameDto)));
    }

    @Then("^The game data updates number to (\\d+)$")
    public void current_player_can_start_playing(final Long number) throws Throwable {
        this.number = number;

        Assertions.assertThat(gameRepository.findByIdOrThrow(gameId))
            .isNotNull()
            .extracting(
                GameServiceDto::getNumber,
                GameServiceDto::isGameStarted)
            .containsExactly(
                number,
                true
            );
    }


    @When("^player 1 take turn to do a \"([^\"]*)\" operation on the number")
    public void other_player_play_turn(final GameDto.OperationDto operation) throws Throwable {
        final GameDto gameDto = GameDto.builder()
            .gameId(gameId)
            .number(number)
            .operation(operation)
            .build();

        latestHttpCall = mockMvc.perform(post(UPDATE_NUMBER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(gameDto)));
    }

    @Then("^The game data number changes to (\\d+)$")
    public void game_data_changes(final Long number) {
        this.number = number;

        Assertions.assertThat(gameRepository.findByIdOrThrow(gameId))
            .isNotNull()
            .extracting(
                GameServiceDto::getNumber,
                GameServiceDto::isGameStarted,
                GameServiceDto::isAutoPlay
            )
            .containsExactly(
                number,
                true,
                false
            );
    }

    @Then("^The game is finished and current player wins$")
    public void game_is_finished() {
        Assertions.assertThat(gameRepository.findByIdOrThrow(gameId))
            .isNotNull()
            .extracting(
                GameServiceDto::getNumber,
                GameServiceDto::isGameStarted,
                GameServiceDto::isAutoPlay,
                GameServiceDto::isWinner,
                GameServiceDto::isFinished
            )
            .containsExactly(
                number,
                true,
                false,
                true,
                true
            );
    }

    @And("^Report player 2 as a loser$")
    public void report_losing_to_the_other_player() {
        verify(postRequestedFor(urlEqualTo("/OTHER_PLAYER_URL" + REPORT_GAME_RESULT_URL))
            .withRequestBody(equalTo("{\"game_id\":\"" + gameId + "\",\"result\":\"LOSER\"}"))
            .withHeader("Content-Type", equalTo("application/json")));

    }

    @Then("^player 1 take turn automatically$")
    public void current_player_automatically_play() {
        final GameServiceDto gameServiceDto = gameRepository.findByIdOrThrow(gameId);

        verify(postRequestedFor(urlEqualTo("/OTHER_PLAYER_URL" + TURN_URL))
            .withRequestBody(equalTo("{\"game_id\":\"1\",\"number\":" + gameServiceDto.getNumber() + "}"))
            .withHeader("Content-Type", equalTo("application/json")));
    }

    @When("^player 1 starts the game with id \"([^\"]*)\" and number (\\d+) and autoPlay \"([^\"]*)\" and communicate it to player 2")
    public void the_user_starts_the_game_on_another_process(final String gameId, final Long number, final boolean isAutoplay) throws Throwable {
        this.number = number;
        this.gameId = gameId;

        final GameDto gameDto = GameDto.builder()
            .gameId(gameId)
            .number(number)
            .autoPlay(isAutoplay)
            .operation(GameDto.OperationDto.ONE)
            .build();

        latestHttpCall = mockMvc.perform(post(SUBMIT_GAME_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(gameDto)));
    }

    @Then("^player 2 receives and handles game and the game data is saved locally$")
    public void other_player_handles_game_submission() {
        final GameServiceDto gameServiceDto = gameRepository.findByIdOrThrow(gameId);

        Assertions.assertThat(gameServiceDto)
            .isNotNull()
            .extracting(
                GameServiceDto::isGameStarted,
                GameServiceDto::isAutoPlay,
                GameServiceDto::getGameId,
                GameServiceDto::getNumber
            ).containsExactly(
                true,
                false,
                gameId,
                number
            );
    }


    @When("^player 2 update number locally by applying operation \"([^\"]*)\"$")
    public void other_player_play_turn_locally(final GameDto.OperationDto operationDto) throws Throwable {
        final GameDto gameDto = GameDto.builder()
            .gameId(gameId)
            .number(number)
            .operation(operationDto)
            .build();

        latestHttpCall = mockMvc.perform(post(UPDATE_NUMBER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(gameDto)));
    }

    @And("^Player 2 receive game result as a loser$")
    public void other_player_is_loser() throws Exception {
        final GameResultsDto gameResultsDto = GameResultsDto.builder()
            .gameId(gameId)
            .result(GameResultsDto.ResultDto.LOSER)
            .build();

        latestHttpCall = mockMvc.perform(post(REPORT_GAME_RESULT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(gameResultsDto)));

    }

    @And("^Player 2 handles the results and saves it locally$")
    public void other_player_handles_game_results() {
        final GameServiceDto gameServiceDto = gameRepository.findByIdOrThrow(gameId);

        Assertions.assertThat(gameServiceDto)
            .isNotNull()
            .extracting(
                GameServiceDto::isGameStarted,
                GameServiceDto::isFinished,
                GameServiceDto::isAutoPlay,
                GameServiceDto::getGameId,
                GameServiceDto::isWinner,
                GameServiceDto::getNumber
            ).containsExactly(
                true,
                true,
                false,
                gameId,
                false,
                1
            );
    }

    private void stubOtherPlayerForReceivingSubmitGame() {
        stubFor(WireMock.post(urlEqualTo("/OTHER_PLAYER_URL" + SUBMIT_GAME_URL))
            .willReturn(aResponse()
                .withStatus(202)));
    }

    private void stubOtherPlayerForReceivingTurn() {
        stubFor(WireMock.post(urlEqualTo("/OTHER_PLAYER_URL" + TURN_URL))
            .willReturn(aResponse()
                .withStatus(202)));
    }

    private void stubOtherPlayerForReceivingGameResults() {
        stubFor(WireMock.post(urlEqualTo("/OTHER_PLAYER_URL" + REPORT_GAME_RESULT_URL))
            .willReturn(aResponse()
                .withStatus(202)));
    }
}
