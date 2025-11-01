package com.netstock.chessadmin.service;

import com.netstock.chessadmin.dto.MatchDTO;
import com.netstock.chessadmin.dto.PlayerDTO;
import com.netstock.chessadmin.entity.Match;
import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.enums.MatchOutcome;
import com.netstock.chessadmin.repository.MatchRepository;
import com.netstock.chessadmin.repository.PlayerRepository;
import com.netstock.chessadmin.service.impl.MatchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class MatchServiceUnitTest {

    @Mock
    PlayerRepository playerRepository;

    @Mock
    ModelMapper modelMapper;

    @Mock
    MatchRepository matchRepository;

    @InjectMocks
    MatchServiceImpl matchService;

    @Test
    void getMatchPlayers_returns_mapped_playerDTOs() {
        Player p = new Player();
        p.setId(1L);
        p.setFirstName("John");
        p.setLastName("Doe");

        PlayerDTO dto = PlayerDTO.builder().id(1L).firstName("John").lastName("Doe").build();

        when(playerRepository.findAll()).thenReturn(List.of(p));
        when(modelMapper.map(p, PlayerDTO.class)).thenReturn(dto);

        var result = matchService.getMatchPlayers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");

        verify(playerRepository).findAll();
        verify(modelMapper).map(p, PlayerDTO.class);
    }

    @Test
    void saveMatch_higherRankedWin_does_not_change_ranks_but_increments_games_played() {
        // higher-ranked player (rank 3) beats lower-ranked (rank 8) -> no rank changes
        PlayerDTO higher = PlayerDTO.builder().id(1L).rank(3).firstName("H").lastName("One").build();
        PlayerDTO lower = PlayerDTO.builder().id(2L).rank(8).firstName("L").lastName("Two").build();

        MatchDTO m = MatchDTO.builder().playerOne(higher).playerTwo(lower).outcome(MatchOutcome.PLAYER_ONE_WON).build();

        // existence checks
        when(playerRepository.existsById(1L)).thenReturn(true);
        when(playerRepository.existsById(2L)).thenReturn(true);

        // mapping from DTO to entity and repository save
        Match matchEntity = new Match();
        matchEntity.setId(10L);
        when(modelMapper.map(any(MatchDTO.class), eq(Match.class))).thenReturn(new Match());
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);

        // mapping saved entity back to DTO (generic stub for any Match)
        MatchDTO returnedDto = MatchDTO.builder().id(10L).build();
        when(modelMapper.map(any(Match.class), eq(MatchDTO.class))).thenReturn(returnedDto);

        // mapping PlayerDTO -> Player for updatePlayersAfterMatch
        Player pHigher = new Player(); pHigher.setId(1L); pHigher.setNumberOfGamesPlayed(0);
        Player pLower = new Player(); pLower.setId(2L); pLower.setNumberOfGamesPlayed(0);
        when(modelMapper.map(higher, Player.class)).thenReturn(pHigher);
        when(modelMapper.map(lower, Player.class)).thenReturn(pLower);

        MatchDTO saved = matchService.saveMatch(m);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(10L);

        // higher-ranked win -> repository rank adjustment methods should NOT be called
        verify(playerRepository, never()).incrementRanksFrom(anyInt(), anyInt());
        verify(playerRepository, never()).decrementRankByOne(anyInt());
        verify(playerRepository, never()).incrementRankByOne(anyInt());

        // players should have their games played incremented and saved
        verify(playerRepository, times(2)).save(any(Player.class));

        // ensure existence checks were performed
        verify(playerRepository).existsById(1L);
        verify(playerRepository).existsById(2L);
    }

    // New edge-case tests
    @Test
    void draw_adjacent_no_rank_change() {
        PlayerDTO higher = PlayerDTO.builder().id(1L).rank(10).firstName("A").lastName("One").build();
        PlayerDTO lower = PlayerDTO.builder().id(2L).rank(11).firstName("B").lastName("Two").build();
        MatchDTO m = MatchDTO.builder().playerOne(higher).playerTwo(lower).outcome(MatchOutcome.DRAW).build();

        when(playerRepository.existsById(1L)).thenReturn(true);
        when(playerRepository.existsById(2L)).thenReturn(true);

        Match savedEntity = new Match(); savedEntity.setId(21L);
        when(modelMapper.map(any(MatchDTO.class), eq(Match.class))).thenReturn(new Match());
        when(matchRepository.save(any(Match.class))).thenReturn(savedEntity);
        when(modelMapper.map(any(Match.class), eq(MatchDTO.class))).thenReturn(MatchDTO.builder().id(21L).build());

        Player p1 = new Player(); p1.setId(1L); p1.setNumberOfGamesPlayed(0);
        Player p2 = new Player(); p2.setId(2L); p2.setNumberOfGamesPlayed(0);
        when(modelMapper.map(higher, Player.class)).thenReturn(p1);
        when(modelMapper.map(lower, Player.class)).thenReturn(p2);

        MatchDTO res = matchService.saveMatch(m);

        assertThat(res).isNotNull();
        // adjacent draw -> no rank increment call
        verify(playerRepository, never()).incrementRankByOne(anyInt());
        verify(playerRepository, times(2)).save(any(Player.class));
    }

    @Test
    void draw_non_adjacent_lower_moves_up_one() {
        PlayerDTO higher = PlayerDTO.builder().id(1L).rank(10).firstName("A").lastName("One").build();
        PlayerDTO lower = PlayerDTO.builder().id(2L).rank(15).firstName("B").lastName("Two").build();
        MatchDTO m = MatchDTO.builder().playerOne(higher).playerTwo(lower).outcome(MatchOutcome.DRAW).build();

        when(playerRepository.existsById(1L)).thenReturn(true);
        when(playerRepository.existsById(2L)).thenReturn(true);

        Match savedEntity = new Match(); savedEntity.setId(22L);
        when(modelMapper.map(any(MatchDTO.class), eq(Match.class))).thenReturn(new Match());
        when(matchRepository.save(any(Match.class))).thenReturn(savedEntity);
        when(modelMapper.map(any(Match.class), eq(MatchDTO.class))).thenReturn(MatchDTO.builder().id(22L).build());

        Player p1 = new Player(); p1.setId(1L); p1.setNumberOfGamesPlayed(0);
        Player p2 = new Player(); p2.setId(2L); p2.setNumberOfGamesPlayed(0);
        when(modelMapper.map(higher, Player.class)).thenReturn(p1);
        when(modelMapper.map(lower, Player.class)).thenReturn(p2);

        MatchDTO res = matchService.saveMatch(m);

        assertThat(res).isNotNull();
        // lower ranked (15) should move up by one -> newRank = 14
        verify(playerRepository).incrementRankByOne(14);
        verify(playerRepository, times(2)).save(any(Player.class));
    }

    @Test
    void upset_non_equal_case_adjusts_ranks_correctly() {
        // higher = 10, lower = 16, lower (playerTwo) wins
        PlayerDTO higher = PlayerDTO.builder().id(1L).rank(10).firstName("H").lastName("One").build();
        PlayerDTO lower = PlayerDTO.builder().id(2L).rank(16).firstName("L").lastName("Two").build();
        MatchDTO m = MatchDTO.builder().playerOne(higher).playerTwo(lower).outcome(MatchOutcome.PLAYER_TWO_WON).build();

        when(playerRepository.existsById(1L)).thenReturn(true);
        when(playerRepository.existsById(2L)).thenReturn(true);

        Match savedEntity = new Match(); savedEntity.setId(30L);
        when(modelMapper.map(any(MatchDTO.class), eq(Match.class))).thenReturn(new Match());
        when(matchRepository.save(any(Match.class))).thenReturn(savedEntity);
        when(modelMapper.map(any(Match.class), eq(MatchDTO.class))).thenReturn(MatchDTO.builder().id(30L).build());

        Player pHigher = new Player(); pHigher.setId(1L); pHigher.setNumberOfGamesPlayed(0);
        Player pLower = new Player(); pLower.setId(2L); pLower.setNumberOfGamesPlayed(0);
        when(modelMapper.map(higher, Player.class)).thenReturn(pHigher);
        when(modelMapper.map(lower, Player.class)).thenReturn(pLower);

        MatchDTO res = matchService.saveMatch(m);

        assertThat(res).isNotNull();
        // expected lower new rank = 13, so incrementRanksFrom(13,16) and decrementRankByOne(11)
        verify(playerRepository).incrementRanksFrom(13, 16);
        verify(playerRepository).decrementRankByOne(11);
        verify(playerRepository, times(2)).save(any(Player.class));
    }

    @Test
    void upset_equal_new_rank_case_handles_tie_by_incrementing_higher() {
        // higher = 1, lower = 3 -> half-diff => lowerNew = 2, higherNew = 2 => tie -> higher++
        PlayerDTO higher = PlayerDTO.builder().id(1L).rank(1).firstName("H").lastName("One").build();
        PlayerDTO lower = PlayerDTO.builder().id(2L).rank(3).firstName("L").lastName("Two").build();
        MatchDTO m = MatchDTO.builder().playerOne(higher).playerTwo(lower).outcome(MatchOutcome.PLAYER_TWO_WON).build();

        when(playerRepository.existsById(1L)).thenReturn(true);
        when(playerRepository.existsById(2L)).thenReturn(true);

        Match savedEntity = new Match(); savedEntity.setId(31L);
        when(modelMapper.map(any(MatchDTO.class), eq(Match.class))).thenReturn(new Match());
        when(matchRepository.save(any(Match.class))).thenReturn(savedEntity);
        when(modelMapper.map(any(Match.class), eq(MatchDTO.class))).thenReturn(MatchDTO.builder().id(31L).build());

        Player pHigher = new Player(); pHigher.setId(1L); pHigher.setNumberOfGamesPlayed(0);
        Player pLower = new Player(); pLower.setId(2L); pLower.setNumberOfGamesPlayed(0);
        when(modelMapper.map(higher, Player.class)).thenReturn(pHigher);
        when(modelMapper.map(lower, Player.class)).thenReturn(pLower);

        MatchDTO res = matchService.saveMatch(m);

        assertThat(res).isNotNull();
        // tie case -> higherPlayerNewRank becomes 3, we expect incrementRanksFrom(3,3) then decrementRankByOne(3)
        verify(playerRepository).incrementRanksFrom(3, 3);
        verify(playerRepository).decrementRankByOne(3);
        verify(playerRepository, times(2)).save(any(Player.class));
    }

    @Test
    void saveMatch_nonExistentPlayer_throwsIllegalArgumentException() {
        PlayerDTO p1 = PlayerDTO.builder().id(99L).rank(1).build();
        PlayerDTO p2 = PlayerDTO.builder().id(2L).rank(2).build();
        MatchDTO m = MatchDTO.builder().playerOne(p1).playerTwo(p2).outcome(MatchOutcome.PLAYER_ONE_WON).build();

        when(playerRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> matchService.saveMatch(m));
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void saveMatch_nullPlayerId_throwsIllegalArgumentException() {
        PlayerDTO p1 = PlayerDTO.builder().id(null).rank(1).build();
        PlayerDTO p2 = PlayerDTO.builder().id(2L).rank(2).build();
        MatchDTO m = MatchDTO.builder().playerOne(p1).playerTwo(p2).outcome(MatchOutcome.PLAYER_ONE_WON).build();

        assertThrows(IllegalArgumentException.class, () -> matchService.saveMatch(m));
        verify(matchRepository, never()).save(any(Match.class));
    }
}
