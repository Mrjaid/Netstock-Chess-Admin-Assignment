package com.netstock.chessadmin.repository;

import com.netstock.chessadmin.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    @Query("SELECT COALESCE(MAX(p.rank), 0) FROM Player p")
    Integer findHighestRank();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Player p SET p.rank = p.rank - 1 WHERE p.rank = :rank")
    void decrementRankByOne(@Param("rank") Integer rank);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Player p SET p.rank = p.rank + 1 WHERE p.rank = :rank")
    void incrementRankByOne(@Param("rank") Integer rank);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Player p SET p.rank = p.rank + 1 WHERE p.rank >= :start AND p.rank < :stop")
    void incrementRanksFrom(@Param("start") int startRank, @Param("stop") int stopRank);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Player p SET p.rank = p.rank - 1 WHERE p.rank > :start AND p.rank <= :stop")
    void decrementRanksFrom(@Param("start") int startRank, @Param("stop") int stopRank);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Player p SET p.rank = :newRank WHERE p.id = :playerId")
    void updatePlayerRankById(@Param("playerId") Long playerId, @Param("newRank") int newRank);

    @Query("SELECT p FROM Player p ORDER BY COALESCE(p.rank) ASC")
    List<Player> findAllOrderByRankAsc();
}
