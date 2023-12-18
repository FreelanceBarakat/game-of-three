package com.just.takeaway.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
public class GameResultsDto extends AbstractGameDto {
    @Builder
    public GameResultsDto(ResultDto result, String gameId) {
        super(gameId);
        this.result = result;
    }

    private ResultDto result;

    public enum ResultDto {
        WINNER, LOSER, ON_GOING;
    }
}
