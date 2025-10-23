package com.netstock.chessadmin.repository;

import com.netstock.chessadmin.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
