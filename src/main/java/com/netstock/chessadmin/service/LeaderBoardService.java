package com.netstock.chessadmin.service;

import com.netstock.chessadmin.entity.Player;

import java.util.List;

public interface LeaderBoardService {
    List<Player> loadPlayersSortedByRank();
}

