package com.netstock.chessadmin.repository;

import com.netstock.chessadmin.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
