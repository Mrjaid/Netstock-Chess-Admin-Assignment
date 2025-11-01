package com.netstock.chessadmin.integration;

import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.repository.MatchRepository;
import com.netstock.chessadmin.repository.PlayerRepository;
import com.netstock.chessadmin.service.LeaderBoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class LeaderBoardServiceTest {

    @Autowired
    LeaderBoardService leaderBoardService;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    MatchRepository matchRepository;

    @BeforeEach
    void setUp() {
        matchRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Test
    void loadPlayersSortedByRank_returns_players_in_ascending_rank() {
        for (int i = 5; i >= 1; i--) {
            Player p = new Player();
            p.setFirstName("P"+i);
            p.setLastName("L"+i);
            p.setEmail("p"+i+"@e.com");
            p.setDateOfBirth(LocalDate.of(1990,1,1));
            p.setRank(i);
            p.setNumberOfGamesPlayed(0);
            playerRepository.save(p);
        }

        List<Player> loaded = leaderBoardService.loadPlayersSortedByRank();
        assertThat(loaded).hasSize(5);
        for (int i = 0; i < loaded.size()-1; i++) {
            assertThat(loaded.get(i).getRank()).isLessThan(loaded.get(i+1).getRank());
        }
    }
}

