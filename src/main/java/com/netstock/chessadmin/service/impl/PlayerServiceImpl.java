package com.netstock.chessadmin.service.impl;

import com.netstock.chessadmin.dto.PlayerDTO;
import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.repository.PlayerRepository;
import com.netstock.chessadmin.service.PlayerService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    private final ModelMapper modelMapper;

    public PlayerServiceImpl(PlayerRepository playerRepository, ModelMapper modelMapper) {
        this.playerRepository = playerRepository;
        this.modelMapper = modelMapper;
    }

    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream().map(player -> modelMapper.map(player, PlayerDTO.class)).collect(Collectors.toList());
    }

    public void save(PlayerDTO playerDTO) {
        playerRepository.save(modelMapper.map(playerDTO, Player.class));
    }

    public void delete(Long playerId) {
        playerRepository.deleteById(playerId);
    }
}
