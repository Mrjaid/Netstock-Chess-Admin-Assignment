package com.netstock.chessadmin.service;

import com.netstock.chessadmin.dto.MatchDTO;
import com.netstock.chessadmin.dto.PlayerDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface MatchService {
    List<PlayerDTO> getMatchPlayers();
    List<MatchDTO> getAllMatches();
    MatchDTO saveMatch(MatchDTO matchDTO);
    Optional<MatchDTO> findById(Long id);
    void deleteMatch(Long id);
}
