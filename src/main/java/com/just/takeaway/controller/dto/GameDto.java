package com.just.takeaway.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
public class GameDto extends AbstractGameDto {
    @NotNull
    private Long number;
    private OperationDto operation;
    private boolean autoPlay;
    private boolean finished;
    private boolean winner;
    private boolean gameStarted;

    @Builder
    public GameDto(String gameId,
                   Long number,
                   OperationDto operation,
                   boolean autoPlay,
                   boolean finished,
                   boolean winner,
                   boolean gameStarted) {
        super(gameId);
        this.number = number;
        this.operation = operation;
        this.autoPlay = autoPlay;
        this.finished = finished;
        this.winner = winner;
        this.gameStarted = gameStarted;
    }


    @Getter
    public enum OperationDto {
        ONE("1"), ZERO("0"), MINUS_ONE("-1");
        private final String value;

        OperationDto(String value) {
            this.value = value;
        }


    }
}
