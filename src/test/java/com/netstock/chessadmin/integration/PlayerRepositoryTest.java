package com.netstock.chessadmin.integration;

import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PlayerRepositoryTest {

    @Autowired
    PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();
        for (int i = 1; i <= 5; i++) {
            Player p = new Player();
            p.setFirstName("First" + i);
            p.setLastName("Last" + i);
            p.setEmail("p" + i + "@example.com");
            p.setDateOfBirth(LocalDate.of(1990, 1, i));
            p.setRank(i);
            p.setNumberOfGamesPlayed(0);
            playerRepository.save(p);
        }
    }

    @Test
    void findAllOrderByRankAsc_returns_sorted() {
        List<Player> players = playerRepository.findAllOrderByRankAsc();
        assertThat(players).hasSize(5);
        assertThat(players.get(0).getRank()).isEqualTo(1);
        assertThat(players.get(4).getRank()).isEqualTo(5);
    }

    @Test
    void incrementRanksFrom_shifts_range_up() {
        // shift ranks 3..5 up by 1 -> players with rank 3..4 become 4..5 (stop exclusive)
        playerRepository.incrementRanksFrom(3, 6);
        List<Player> players = playerRepository.findAllOrderByRankAsc();
        assertThat(players.stream().map(Player::getRank)).containsExactly(1,2,4,5,6);
    }

    @Test
    void updatePlayerRankById_updates_specific_player() {
        Player p3 = playerRepository.findAllOrderByRankAsc().get(2);
        playerRepository.updatePlayerRankById(p3.getId(), 10);
        Player updated = playerRepository.findById(p3.getId()).orElseThrow();
        assertThat(updated.getRank()).isEqualTo(10);
    }
}

