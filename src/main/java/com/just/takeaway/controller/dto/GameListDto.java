package com.just.takeaway.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameListDto {
    private List<GameDto> games;
}
