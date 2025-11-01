package com.netstock.chessadmin.integration;

import com.netstock.chessadmin.dto.PlayerDTO;
import com.netstock.chessadmin.entity.Match;
import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.repository.MatchRepository;
import com.netstock.chessadmin.repository.PlayerRepository;
import com.netstock.chessadmin.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PlayerServiceTest {

    @Autowired
    PlayerService playerService;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        matchRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Test
    void savePlayer_creates_player_in_db() {
        PlayerDTO dto = PlayerDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .dateOfBirth(LocalDate.of(1990,1,1))
                .rank(1)
                .build();

        playerService.save(dto);

        List<Player> all = playerRepository.findAll();
        assertThat(all).hasSize(1);
        Player p = all.get(0);
        assertThat(p.getFirstName()).isEqualTo("John");
        assertThat(p.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void deletePlayer_clears_match_references_and_deletes_player() {
        // create two players
        Player a = new Player();
        a.setFirstName("A"); a.setLastName("One"); a.setEmail("a@e.com"); a.setDateOfBirth(LocalDate.of(1990,1,1)); a.setRank(1); a.setNumberOfGamesPlayed(0);
        Player b = new Player();
        b.setFirstName("B"); b.setLastName("Two"); b.setEmail("b@e.com"); b.setDateOfBirth(LocalDate.of(1990,1,1)); b.setRank(2); b.setNumberOfGamesPlayed(0);
        playerRepository.save(a);
        playerRepository.save(b);

        // create a match referencing player a as playerOne
        Match m = new Match();
        m.setOutcome(null);
        m.setPlayerOneId(a.getId());
        m.setPlayerTwoId(b.getId());
        matchRepository.save(m);

        // sanity
        assertThat(playerRepository.existsById(a.getId())).isTrue();
        assertThat(matchRepository.existsByPlayerOneIdOrPlayerTwoId(a.getId(), a.getId())).isTrue();

        // delete player a via service
        playerService.delete(a.getId());

        // player should be gone and match references cleared
        assertThat(playerRepository.existsById(a.getId())).isFalse();
        assertThat(matchRepository.existsByPlayerOneIdOrPlayerTwoId(a.getId(), a.getId())).isFalse();
    }
}

