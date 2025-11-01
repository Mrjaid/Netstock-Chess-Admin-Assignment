package com.netstock.chessadmin.service.impl;

import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.repository.PlayerRepository;
import com.netstock.chessadmin.service.LeaderBoardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaderBoardServiceImpl implements LeaderBoardService {
    private final PlayerRepository playerRepository;

    public LeaderBoardServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<Player> loadPlayersSortedByRank() {
        return playerRepository.findAllOrderByRankAsc();
    }
}
