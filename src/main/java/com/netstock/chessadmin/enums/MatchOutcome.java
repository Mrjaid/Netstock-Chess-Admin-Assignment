package com.netstock.chessadmin.enums;

import lombok.Getter;

@Getter
public enum MatchOutcome {
    PLAYER_ONE_WON("Player one won"),
    PLAYER_TWO_WON("Player two won"),
    DRAW("Draw");
    private final String outcome;
    MatchOutcome(String outcome) {
        this.outcome = outcome;
    }
}