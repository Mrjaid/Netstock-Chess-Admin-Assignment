package com.netstock.chessadmin.service;

import com.netstock.chessadmin.dto.PlayerDTO;

import java.util.List;

public interface PlayerService {
    List<PlayerDTO> getAllPlayers();

    void save(PlayerDTO player);

    void delete(Long playerId);
}
