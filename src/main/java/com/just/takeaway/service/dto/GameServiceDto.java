package com.just.takeaway.service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Slf4j
public class GameServiceDto {
    public static final int WINNER_NUMBER = 1;
    private final String gameId;

    private boolean waitingForTurn;
    private boolean isAutoPlay;
    private Long number;
    private Operation operation;
    private boolean isFinished;
    private boolean isWinner;
    private boolean isGameStarted;

    public GameServiceDto updateNumber() {

        if (isGameFinished()) {
            return this;
        }

        applyOperation();

        number /= 3;
        log.info("Number={} after dividing by 3", number);

        isGameFinished();
        return this;

    }

    private boolean isGameFinished() {
        if (number == WINNER_NUMBER) {
            isWinner = true;
            isFinished = true;
            return true;
        }

        return false;
    }

    private void applyOperation() {
        if (isAutoPlay) {
            final int choice = (int) (Math.random() * 3);

            number = switch (choice) {
                case 0 -> incrementNumber();
                case 1 -> decrementNumber();
                default -> addZero();
            };
        } else {
            number = switch (operation) {
                case ONE -> incrementNumber();
                case MINUS_ONE -> decrementNumber();
                case ZERO -> addZero();
            };
        }
    }

    private Long addZero() {
        log.info("Current number={}, after applying operation of adding zero", number);
        return number;
    }

    private long decrementNumber() {
        log.info("Result number={}, after applying operation of subtracting 1", number - 1);
        return number - 1;
    }

    private long incrementNumber() {
        log.info("Current number={}, after applying operation of adding 1", number + 1);
        return number + 1;
    }


    @Getter
    public enum Operation {
        ONE(1), ZERO(0), MINUS_ONE(-1);
        private final int value;

        Operation(int value) {
            this.value = value;
        }


    }
}
