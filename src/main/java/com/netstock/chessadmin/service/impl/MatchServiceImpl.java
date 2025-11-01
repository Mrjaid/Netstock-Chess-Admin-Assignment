package com.netstock.chessadmin.service.impl;

import com.netstock.chessadmin.dto.MatchDTO;
import com.netstock.chessadmin.dto.PlayerDTO;
import com.netstock.chessadmin.entity.Match;
import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.enums.MatchOutcome;
import com.netstock.chessadmin.repository.MatchRepository;
import com.netstock.chessadmin.repository.PlayerRepository;
import com.netstock.chessadmin.service.MatchService;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

    public List<PlayerDTO> getMatchPlayers() {
        return playerRepository.findAll().stream().map(player ->
                modelMapper.map(player, PlayerDTO.class)).collect(Collectors.toList());
    }

    public List<MatchDTO> getAllMatches() {
        List<Match> matches = matchRepository.findAllWithPlayers();
        return matches.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public MatchDTO saveMatch(@Valid MatchDTO matchDTO) {
        Long playerOneId = matchDTO.getPlayerOne().getId();
        Long playerTwoId = matchDTO.getPlayerTwo().getId();
        validatePlayersExists(playerOneId, playerTwoId);
        Match saved = saveMatchEntity(matchDTO, playerOneId, playerTwoId);
        // Update ranks and players
        updatePlayerRankings(matchDTO);
        updatePlayersAfterMatch(matchDTO.getPlayerOne(), matchDTO.getPlayerTwo());
        return toDto(saved);
    }

    @Transactional
    public Optional<MatchDTO> findById(Long id) {
        return matchRepository.findByIdWithPlayers(id).map(this::toDto);
    }

    @Transactional
    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }

    private Match saveMatchEntity(MatchDTO matchDTO, Long p1Id, Long p2Id) {
        Match match = modelMapper.map(matchDTO, Match.class);
        match.setPlayerOneId(p1Id);
        match.setPlayerTwoId(p2Id);
        match.setCreatedAt(Instant.now());
        return matchRepository.save(match);
    }

    private void validatePlayersExists(Long playerOneId, Long playerTwoId) {
        if (null == playerOneId || !playerRepository.existsById(playerOneId)) {
            throw new IllegalArgumentException("playerOne does not exist");
        }
        if (null == playerTwoId || !playerRepository.existsById(playerTwoId)) {
            throw new IllegalArgumentException("playerTwo does not exist");
        }
    }

    private void updatePlayerRankings(MatchDTO matchDTO) {
        if (higherRankedPlayerWon(matchDTO)) {
            return;
        }
        if(matchDTO.getOutcome().equals(MatchOutcome.DRAW)) {
            adjustRankingsForDraw(matchDTO);
        } else {
            adjustRankingsForUpset(matchDTO);
        }
    }

    private void adjustRankingsForDraw(MatchDTO matchDTO) {
        PlayerDTO higherRankedPlayer = getHigherRankedPlayer(matchDTO);
        PlayerDTO lowerRankedPlayer = getLowerRankedPlayer(matchDTO);
        int rankDifference = lowerRankedPlayer.getRank() - higherRankedPlayer.getRank();
        if (rankDifference > 1) {
            int newRank = lowerRankedPlayer.getRank() - 1;
            playerRepository.incrementRankByOne(newRank);
            lowerRankedPlayer.setRank(newRank);
        }
    }

    private void updatePlayersAfterMatch(PlayerDTO lowerRankedPlayer, PlayerDTO higherRankedPlayer) {
        Player playerOne = modelMapper.map(lowerRankedPlayer, Player.class);
        Player playerTwo = modelMapper.map(higherRankedPlayer, Player.class);
        playerOne.setNumberOfGamesPlayed(playerOne.getNumberOfGamesPlayed() + 1);
        playerTwo.setNumberOfGamesPlayed(playerTwo.getNumberOfGamesPlayed() + 1);
        playerRepository.save(playerOne);
        playerRepository.save(playerTwo);
    }

    private  void adjustRankingsForUpset(MatchDTO matchDTO) {
        PlayerDTO higherRankedPlayer = getHigherRankedPlayer(matchDTO);
        PlayerDTO lowerRankedPlayer = getLowerRankedPlayer(matchDTO);
        int higherPlayerNewRank = higherRankedPlayer.getRank() + 1;
        int lowerPlayerNewRank = getLowerRankedPlayerNewRank(higherRankedPlayer.getRank(), lowerRankedPlayer.getRank());
        if (higherPlayerNewRank == lowerPlayerNewRank) {
            ++higherPlayerNewRank;
            playerRepository.incrementRanksFrom(higherPlayerNewRank, lowerRankedPlayer.getRank());
        } else {
            playerRepository.incrementRanksFrom(lowerPlayerNewRank, lowerRankedPlayer.getRank());
        }
        playerRepository.decrementRankByOne(higherPlayerNewRank);
        lowerRankedPlayer.setRank(lowerPlayerNewRank);
        higherRankedPlayer.setRank(higherPlayerNewRank);
    }

    private int getLowerRankedPlayerNewRank(int higherRank, int lowerRank) {
        return lowerRank - (((lowerRank - higherRank) + 1 )/ 2) ;
    }

    private boolean higherRankedPlayerWon(MatchDTO matchDTO) {
        return ((MatchOutcome.PLAYER_ONE_WON.equals(matchDTO.getOutcome()))
                && (matchDTO.getPlayerOne().getRank() < matchDTO.getPlayerTwo().getRank()))
                || ((MatchOutcome.PLAYER_TWO_WON.equals(matchDTO.getOutcome()))
                && (matchDTO.getPlayerTwo().getRank() < matchDTO.getPlayerOne().getRank()));
    }

    @NotNull
    private PlayerDTO getHigherRankedPlayer(MatchDTO matchDTO) {
        return (matchDTO.getPlayerOne().getRank() > matchDTO.getPlayerTwo().getRank()) ?
                matchDTO.getPlayerTwo() : matchDTO.getPlayerOne();
    }

    private PlayerDTO getLowerRankedPlayer(MatchDTO matchDTO) {
        return (matchDTO.getPlayerOne().getRank() > matchDTO.getPlayerTwo().getRank()) ?
                matchDTO.getPlayerOne() : matchDTO.getPlayerTwo();
    }

    private MatchDTO toDto(Match match) {
        if (null == match) return null;
        MatchDTO dto = modelMapper.map(match, MatchDTO.class);
        if (null != match.getPlayerOne()) dto.setPlayerOne(modelMapper.map(match.getPlayerOne(), PlayerDTO.class));
        if (null != match.getPlayerTwo()) dto.setPlayerTwo(modelMapper.map(match.getPlayerTwo(), PlayerDTO.class));
        return dto;
    }
}
