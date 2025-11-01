package com.netstock.chessadmin.dto;

import com.netstock.chessadmin.enums.MatchOutcome;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private Long id;

    @NotNull
    private PlayerDTO playerOne;
    @NotNull
    private PlayerDTO playerTwo;
    @NotNull
    private MatchOutcome outcome;
}
