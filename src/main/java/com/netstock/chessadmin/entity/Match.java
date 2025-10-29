package com.netstock.chessadmin.entity;

import com.netstock.chessadmin.enums.MatchOutcome;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Match extends BaseEntity {

    private MatchOutcome outcome;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerOneId")
    private Player playerOne;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerTwoId")
    private Player playerTwo;
}
