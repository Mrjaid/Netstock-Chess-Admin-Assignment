package com.netstock.chessadmin.integration;

import com.netstock.chessadmin.dto.MatchDTO;
import com.netstock.chessadmin.dto.PlayerDTO;
import com.netstock.chessadmin.entity.Match;
import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.enums.MatchOutcome;
import com.netstock.chessadmin.repository.MatchRepository;
import com.netstock.chessadmin.repository.PlayerRepository;
import com.netstock.chessadmin.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MatchServiceTest {

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    MatchService matchService;

    @Autowired
    ModelMapper modelMapper;

    @BeforeEach
    void cleanDb() {
        matchRepository.deleteAll();
        playerRepository.deleteAll();
    }

    // Helper to seed N players with ranks 1..N
    private void seedPlayers(int n) {
        for (int i = 1; i <= n; i++) {
            Player p = new Player();
            p.setFirstName("P" + i);
            p.setLastName("L" + i);
            p.setEmail("p" + i + "@e.com");
            p.setDateOfBirth(LocalDate.of(1990, 1, 1));
            p.setRank(i);
            p.setNumberOfGamesPlayed(0);
            playerRepository.save(p);
        }
    }

    @Test
    void upset_case_rank_adjustment_and_matches_incremented() {
        seedPlayers(16);
        List<Player> players = playerRepository.findAllOrderByRankAsc();
        Player p1 = players.get(0); // rank 1
        Player p15 = players.get(14); // rank 15

        PlayerDTO dto1 = modelMapper.map(p1, PlayerDTO.class);
        PlayerDTO dto15 = modelMapper.map(p15, PlayerDTO.class);

        MatchDTO matchDTO = MatchDTO.builder().playerOne(dto15).playerTwo(dto1).outcome(MatchOutcome.PLAYER_ONE_WON).build();

        matchService.saveMatch(matchDTO);

        // reload
        List<Player> after = playerRepository.findAllOrderByRankAsc();
        // verify matches played incremented
        Player updated1 = playerRepository.findById(p1.getId()).orElseThrow();
        Player updated15 = playerRepository.findById(p15.getId()).orElseThrow();
        assertThat(updated1.getNumberOfGamesPlayed()).isEqualTo(1);
        assertThat(updated15.getNumberOfGamesPlayed()).isEqualTo(1);
        // ranks should be unique
        long distinct = after.stream().map(Player::getRank).distinct().count();
        assertThat(distinct).isEqualTo(after.size());
    }

    @Test
    void draw_adjacent_no_change() {
        seedPlayers(20);
        List<Player> players = playerRepository.findAllOrderByRankAsc();
        Player p10 = players.get(9); // rank 10
        Player p11 = players.get(10); // rank 11

        PlayerDTO dto10 = modelMapper.map(p10, PlayerDTO.class);
        PlayerDTO dto11 = modelMapper.map(p11, PlayerDTO.class);

        MatchDTO match = MatchDTO.builder().playerOne(dto10).playerTwo(dto11).outcome(MatchOutcome.DRAW).build();
        matchService.saveMatch(match);

        Player re10 = playerRepository.findById(p10.getId()).orElseThrow();
        Player re11 = playerRepository.findById(p11.getId()).orElseThrow();
        assertThat(re10.getRank()).isEqualTo(10);
        assertThat(re11.getRank()).isEqualTo(11);
    }

    @Test
    void draw_non_adjacent_lower_moves_up_by_one() {
        seedPlayers(20);
        List<Player> players = playerRepository.findAllOrderByRankAsc();
        Player p5 = players.get(4); // rank 5
        Player p12 = players.get(11); // rank 12

        PlayerDTO dto5 = modelMapper.map(p5, PlayerDTO.class);
        PlayerDTO dto12 = modelMapper.map(p12, PlayerDTO.class);

        MatchDTO match = MatchDTO.builder().playerOne(dto5).playerTwo(dto12).outcome(MatchOutcome.DRAW).build();
        matchService.saveMatch(match);

        Player re5 = playerRepository.findById(p5.getId()).orElseThrow();
        Player re12 = playerRepository.findById(p12.getId()).orElseThrow();
        assertThat(re5.getRank()).isEqualTo(5);
        assertThat(re12.getRank()).isEqualTo(11);

        // ensure ranks remain unique
        List<Player> after = playerRepository.findAllOrderByRankAsc();
        long distinct = after.stream().map(Player::getRank).distinct().count();
        assertThat(distinct).isEqualTo(after.size());
    }

    @Test
    void higher_ranked_win_no_change() {
        seedPlayers(20);
        List<Player> players = playerRepository.findAllOrderByRankAsc();
        Player p3 = players.get(2); // rank 3
        Player p8 = players.get(7); // rank 8

        PlayerDTO dto3 = modelMapper.map(p3, PlayerDTO.class);
        PlayerDTO dto8 = modelMapper.map(p8, PlayerDTO.class);

        MatchDTO match = MatchDTO.builder().playerOne(dto3).playerTwo(dto8).outcome(MatchOutcome.PLAYER_ONE_WON).build();
        matchService.saveMatch(match);

        Player re3 = playerRepository.findById(p3.getId()).orElseThrow();
        Player re8 = playerRepository.findById(p8.getId()).orElseThrow();
        assertThat(re3.getRank()).isEqualTo(3);
        assertThat(re8.getRank()).isEqualTo(8);
    }

    @Test
    void upset_lower_wins_adjusts_ranks_correctly() {
        seedPlayers(20);
        List<Player> players = playerRepository.findAllOrderByRankAsc();
        Player p4 = players.get(3); // rank 4
        Player p14 = players.get(13); // rank 14

        PlayerDTO dto4 = modelMapper.map(p4, PlayerDTO.class);
        PlayerDTO dto14 = modelMapper.map(p14, PlayerDTO.class);

        MatchDTO match = MatchDTO.builder().playerOne(dto14).playerTwo(dto4).outcome(MatchOutcome.PLAYER_ONE_WON).build();
        matchService.saveMatch(match);

        Player after4 = playerRepository.findById(p4.getId()).orElseThrow();
        Player after14 = playerRepository.findById(p14.getId()).orElseThrow();

        int expectedHigherNew = 4 + 1; // 5
        int expectedLowerNew = 14 - (((14 - 4) + 1) / 2); // per service formula

        assertThat(after4.getRank()).isEqualTo(expectedHigherNew);
        assertThat(after14.getRank()).isEqualTo(expectedLowerNew);

        // ensure ranks unique
        List<Player> after = playerRepository.findAllOrderByRankAsc();
        long distinct = after.stream().map(Player::getRank).distinct().count();
        assertThat(distinct).isEqualTo(after.size());

        // matches played incremented
        assertThat(after4.getNumberOfGamesPlayed()).isEqualTo(1);
        assertThat(after14.getNumberOfGamesPlayed()).isEqualTo(1);
    }

    @Test
    void saveMatch_returnsDto_and_findById_returnsMatchWithPlayers() {
        // seed minimal players
        Player pA = new Player();
        pA.setFirstName("Alice");
        pA.setLastName("One");
        pA.setEmail("a@e.com");
        pA.setDateOfBirth(LocalDate.of(1990,1,1));
        pA.setRank(1);
        pA.setNumberOfGamesPlayed(0);
        playerRepository.save(pA);

        Player pB = new Player();
        pB.setFirstName("Bob");
        pB.setLastName("Two");
        pB.setEmail("b@e.com");
        pB.setDateOfBirth(LocalDate.of(1990,1,1));
        pB.setRank(2);
        pB.setNumberOfGamesPlayed(0);
        playerRepository.save(pB);

        PlayerDTO dto1 = modelMapper.map(pA, PlayerDTO.class);
        PlayerDTO dto2 = modelMapper.map(pB, PlayerDTO.class);

        MatchDTO toSave = MatchDTO.builder().playerOne(dto1).playerTwo(dto2).outcome(MatchOutcome.PLAYER_ONE_WON).build();

        MatchDTO saved = matchService.saveMatch(toSave);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        Optional<MatchDTO> found = matchService.findById(saved.getId());
        assertThat(found).isPresent();
        MatchDTO foundDto = found.get();
        assertThat(foundDto.getPlayerOne()).isNotNull();
        assertThat(foundDto.getPlayerTwo()).isNotNull();
        assertThat(foundDto.getPlayerOne().getFirstName()).isEqualTo("Alice");
        assertThat(foundDto.getPlayerTwo().getFirstName()).isEqualTo("Bob");
    }

    @Test
    void deleteMatch_removesEntry_but_playersRemain() {
        // create players
        Player pA = new Player();
        pA.setFirstName("Alice");
        pA.setLastName("One");
        pA.setEmail("a@e.com");
        pA.setDateOfBirth(LocalDate.of(1990,1,1));
        pA.setRank(1);
        pA.setNumberOfGamesPlayed(0);
        playerRepository.save(pA);

        Player pB = new Player();
        pB.setFirstName("Bob");
        pB.setLastName("Two");
        pB.setEmail("b@e.com");
        pB.setDateOfBirth(LocalDate.of(1990,1,1));
        pB.setRank(2);
        pB.setNumberOfGamesPlayed(0);
        playerRepository.save(pB);

        PlayerDTO dto1 = modelMapper.map(pA, PlayerDTO.class);
        PlayerDTO dto2 = modelMapper.map(pB, PlayerDTO.class);

        MatchDTO saved = matchService.saveMatch(MatchDTO.builder().playerOne(dto1).playerTwo(dto2).outcome(MatchOutcome.PLAYER_ONE_WON).build());
        Long matchId = saved.getId();
        assertThat(matchRepository.findById(matchId)).isPresent();

        matchService.deleteMatch(matchId);

        assertThat(matchRepository.findById(matchId)).isNotPresent();
        // players still exist
        assertThat(playerRepository.findById(pA.getId())).isPresent();
        assertThat(playerRepository.findById(pB.getId())).isPresent();
    }

    @Test
    void savedMatch_entity_contains_playerIds_and_createdAt() {
        Player pA = new Player();
        pA.setFirstName("Alice");
        pA.setLastName("One");
        pA.setEmail("a@e.com");
        pA.setDateOfBirth(LocalDate.of(1990,1,1));
        pA.setRank(1);
        pA.setNumberOfGamesPlayed(0);
        playerRepository.save(pA);

        Player pB = new Player();
        pB.setFirstName("Bob");
        pB.setLastName("Two");
        pB.setEmail("b@e.com");
        pB.setDateOfBirth(LocalDate.of(1990,1,1));
        pB.setRank(2);
        pB.setNumberOfGamesPlayed(0);
        playerRepository.save(pB);

        PlayerDTO dto1 = modelMapper.map(pA, PlayerDTO.class);
        PlayerDTO dto2 = modelMapper.map(pB, PlayerDTO.class);

        MatchDTO saved = matchService.saveMatch(MatchDTO.builder().playerOne(dto1).playerTwo(dto2).outcome(MatchOutcome.PLAYER_ONE_WON).build());
        assertThat(saved.getId()).isNotNull();

        Optional<Match> entity = matchRepository.findById(saved.getId());
        assertThat(entity).isPresent();
        Match match = entity.get();
        assertThat(match.getPlayerOneId()).isEqualTo(pA.getId());
        assertThat(match.getPlayerTwoId()).isEqualTo(pB.getId());
        assertThat(match.getCreatedAt()).isNotNull();
    }
}

