package com.netstock.chessadmin.service.impl;

import com.netstock.chessadmin.dto.MatchDTO;
import com.netstock.chessadmin.dto.MatchPlayerDTO;
import com.netstock.chessadmin.repository.MatchRepository;
import com.netstock.chessadmin.repository.PlayerRepository;
import com.netstock.chessadmin.service.MatchService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {
    private final PlayerRepository playerRepository;
    private final ModelMapper modelMapper;
    private final MatchRepository matchRepository;

    public MatchServiceImpl(
            PlayerRepository playerRepository,
            ModelMapper modelMapper,
            MatchRepository matchRepository) {
        this.playerRepository = playerRepository;
        this.modelMapper = modelMapper;
        this.matchRepository = matchRepository;
    }

    public List<MatchPlayerDTO> getMatchPlayers() {
        return playerRepository.findAll().stream().map(player -> modelMapper.map(player, MatchPlayerDTO.class)).collect(Collectors.toList());
    }

    public List<MatchDTO> getAllMatches() {
        return matchRepository.findAll().stream().map(match -> modelMapper.map(match, MatchDTO.class)).collect(Collectors.toList());
    }
}
