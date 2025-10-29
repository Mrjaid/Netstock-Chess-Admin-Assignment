package com.netstock.chessadmin.service;

import com.netstock.chessadmin.dto.MatchDTO;
import com.netstock.chessadmin.dto.MatchPlayerDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MatchService {
    List<MatchPlayerDTO> getMatchPlayers();
    List<MatchDTO> getAllMatches();
}
