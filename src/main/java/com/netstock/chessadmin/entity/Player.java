package com.netstock.chessadmin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "player", indexes = {@Index(name = "idx_rank", columnList = "rank")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private long numberOfGamesPlayed;
    private int rank;
    private Instant createdAt;
}
