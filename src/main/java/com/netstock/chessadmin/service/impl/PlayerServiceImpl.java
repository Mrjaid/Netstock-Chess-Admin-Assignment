package com.netstock.chessadmin.service.impl;

import com.netstock.chessadmin.dto.PlayerDTO;
import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.repository.MatchRepository;
import com.netstock.chessadmin.repository.PlayerRepository;
import com.netstock.chessadmin.service.PlayerService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final ModelMapper modelMapper;

    public PlayerServiceImpl(PlayerRepository playerRepository, MatchRepository matchRepository, ModelMapper modelMapper) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.modelMapper = modelMapper;
    }

    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream().map(player -> modelMapper.map(player, PlayerDTO.class)).collect(Collectors.toList());
    }

    public void save(PlayerDTO playerDTO) {
        if (Objects.nonNull(playerDTO.getId())) {
            updatePlayer(playerDTO);
        } else {
            saveNewPlayer(playerDTO);
        }
    }

    private void saveNewPlayer(PlayerDTO playerDTO) {
        Player player = modelMapper.map(playerDTO, Player.class);
        player.setRank(playerRepository.findHighestRank() + 1);
        playerRepository.save(player);
    }

    private void updatePlayer(PlayerDTO playerDTO) {
        Player player = modelMapper.map(playerDTO, Player.class);
        Player existingPlayer = playerRepository.findById(playerDTO.getId()).orElseThrow(()-> new IllegalArgumentException("Update Player not found"));
        if (Objects.nonNull(existingPlayer)) {
            player.setRank(existingPlayer.getRank());
            playerRepository.save(player);
        }
    }

    @Transactional
    public void delete(Long playerId) {
        int highestRank = playerRepository.findHighestRank();
        Player player = playerRepository.findById(playerId).orElse(null);
        matchRepository.clearPlayerReferences(playerId);
        playerRepository.deleteById(playerId);
        if (Objects.nonNull(player) && player.getRank() > highestRank) {
            playerRepository.decrementRanksFrom(player.getRank(), highestRank);
        }
    }
}
