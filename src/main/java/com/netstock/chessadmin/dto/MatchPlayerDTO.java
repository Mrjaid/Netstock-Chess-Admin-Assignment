package com.netstock.chessadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MatchPlayerDTO {
    Long id;
    private String firstName;
    private String lastName;
    private long currentRank;
    private long newRank;
}