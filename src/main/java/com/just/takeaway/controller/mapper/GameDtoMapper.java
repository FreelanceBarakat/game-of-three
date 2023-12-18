package com.just.takeaway.controller.mapper;

import com.just.takeaway.controller.dto.GameDto;
import com.just.takeaway.controller.dto.GameListDto;
import com.just.takeaway.controller.dto.GameResultsDto;
import com.just.takeaway.controller.dto.TurnDto;
import com.just.takeaway.service.dto.GameServiceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import static com.just.takeaway.controller.dto.GameResultsDto.ResultDto.LOSER;
import static com.just.takeaway.controller.dto.GameResultsDto.ResultDto.ON_GOING;
import static com.just.takeaway.controller.dto.GameResultsDto.ResultDto.WINNER;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface GameDtoMapper {
    @Mapping(target = "waitingForTurn", ignore = true)
    @Mapping(target = "isGameStarted", ignore = true)
    @Mapping(target = "isWinner", ignore = true)
    @Mapping(target = "isFinished", ignore = true)
    @Mapping(target = "isAutoPlay", source = "gameDto.autoPlay")
    GameServiceDto toGameServiceDto(final GameDto gameDto);

    @Mapping(target = "waitingForTurn", ignore = true)
    @Mapping(target = "operation", ignore = true)
    @Mapping(target = "isGameStarted", ignore = true)
    @Mapping(target = "isAutoPlay", ignore = true)
    @Mapping(target = "isWinner", ignore = true)
    @Mapping(target = "isFinished", ignore = true)
    GameServiceDto toGameServiceDto(final TurnDto turnDto);

    GameDto toGameDto(final GameServiceDto gameServiceDto);

    TurnDto toTurnDto(final GameServiceDto gameServiceDto);

    List<GameDto> toGameDto(final List<GameServiceDto> gameServiceDto);

    @Mapping(target = "result", expression = "java(com.just.takeaway.controller.dto.GameResultsDto.ResultDto.LOSER)")
    GameResultsDto toLoserGameResultsDto(final GameServiceDto gameServiceDto);

    default GameListDto toGameDtoList(final List<GameServiceDto> gameServiceDto) {
        return GameListDto.builder()
            .games(toGameDto(gameServiceDto))
            .build();
    }

    default GameResultsDto toGameResultsDto(final GameServiceDto gameServiceDto) {
        GameResultsDto.ResultDto result;

        if (gameServiceDto.isWinner()) {
            result = WINNER;
        } else if (!gameServiceDto.isWinner() && gameServiceDto.isFinished()) {
            result = LOSER;
        } else {
            result = ON_GOING;
        }

        return GameResultsDto.builder()
            .gameId(gameServiceDto.getGameId())
            .result(result)
            .build();
    }

}
