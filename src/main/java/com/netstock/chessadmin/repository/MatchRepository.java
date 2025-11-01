package com.netstock.chessadmin.repository;

import com.netstock.chessadmin.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("select m from Match m left join fetch m.playerOne left join fetch m.playerTwo where m.id = :id")
    Optional<Match> findByIdWithPlayers(@Param("id") Long id);

    @Query("select distinct m from Match m left join fetch m.playerOne left join fetch m.playerTwo")
    List<Match> findAllWithPlayers();

    /**
     * Clear references to a player by setting playerOneId/playerTwoId to NULL where they match the given id.
     * Requires that the playerOneId/playerTwoId columns are nullable.
     * Returns number of rows updated. Must be invoked inside a transaction.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Match m SET "
            + "m.playerOneId = CASE WHEN m.playerOneId = :playerId THEN NULL ELSE m.playerOneId END, "
            + "m.playerTwoId = CASE WHEN m.playerTwoId = :playerId THEN NULL ELSE m.playerTwoId END "
            + "WHERE m.playerOneId = :playerId OR m.playerTwoId = :playerId")
    void clearPlayerReferences(@Param("playerId") Long playerId);

    boolean existsByPlayerOneIdOrPlayerTwoId(Long playerOneId, Long playerTwoId);
}
