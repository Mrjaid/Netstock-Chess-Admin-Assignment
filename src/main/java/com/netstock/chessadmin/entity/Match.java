package com.netstock.chessadmin.entity;

import com.netstock.chessadmin.enums.MatchOutcome;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private MatchOutcome outcome;

    @Column(nullable = true)
    private Long playerOneId;

    @Column(nullable = true)
    private Long playerTwoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerOneId", insertable = false, updatable = false)
    private Player playerOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerTwoId", insertable = false, updatable = false)
    private Player playerTwo;
    private Instant createdAt;
}
