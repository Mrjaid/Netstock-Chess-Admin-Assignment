package com.netstock.chessadmin.service;

import com.netstock.chessadmin.dto.PlayerDTO;
import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.repository.MatchRepository;
import com.netstock.chessadmin.repository.PlayerRepository;
import com.netstock.chessadmin.service.impl.PlayerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceUnitTest {

    @Mock
    PlayerRepository playerRepository;

    @Mock
    MatchRepository matchRepository;

    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    PlayerServiceImpl playerService;

    @Test
    void getAllPlayers_mapsEntitiesToDTOs() {
        Player p = new Player();
        p.setId(5L);
        p.setFirstName("Jane");
        p.setLastName("Smith");

        PlayerDTO dto = PlayerDTO.builder().id(5L).firstName("Jane").lastName("Smith").build();

        when(playerRepository.findAll()).thenReturn(List.of(p));
        when(modelMapper.map(p, PlayerDTO.class)).thenReturn(dto);

        var result = playerService.getAllPlayers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L);
        assertThat(result.get(0).getFirstName()).isEqualTo("Jane");

        verify(playerRepository).findAll();
        verify(modelMapper).map(p, PlayerDTO.class);
    }

    @Test
    void save_new_player_assigns_highestRankPlusOne_and_saves() {
        PlayerDTO newDto = PlayerDTO.builder().firstName("New").lastName("Player").build();
        Player mapped = new Player();

        when(modelMapper.map(newDto, Player.class)).thenReturn(mapped);
        when(playerRepository.findHighestRank()).thenReturn(10);

        playerService.save(newDto);

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(captor.capture());
        Player saved = captor.getValue();
        assertThat(saved.getRank()).isEqualTo(11);
    }

    @Test
    void update_existing_player_keeps_rank_from_existing_record() {
        PlayerDTO updateDto = PlayerDTO.builder().id(2L).firstName("Up").lastName("Date").build();
        Player mapped = new Player();
        mapped.setId(2L);
        mapped.setFirstName("Up");

        Player existing = new Player();
        existing.setId(2L);
        existing.setRank(7);

        when(modelMapper.map(updateDto, Player.class)).thenReturn(mapped);
        when(playerRepository.findById(2L)).thenReturn(Optional.of(existing));

        playerService.save(updateDto);

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(captor.capture());
        Player saved = captor.getValue();
        // rank should be preserved from existing
        assertThat(saved.getRank()).isEqualTo(7);
    }
}

