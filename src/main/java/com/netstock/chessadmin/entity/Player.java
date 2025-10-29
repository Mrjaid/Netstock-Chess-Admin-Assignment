package com.netstock.chessadmin.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Player extends BaseEntity {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private long numberOfGamesPlayed;
    private long currentRank;
}
