package com.netstock.chessadmin.view;

import com.netstock.chessadmin.entity.Player;
import com.netstock.chessadmin.service.LeaderBoardService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@PageTitle("Leader Board")
@Route(value = "leaderboard", layout = MainLayout.class)
public class LeaderBoardView extends VerticalLayout {

    private final LeaderBoardService leaderBoardService;
    private final Grid<Player> grid = new Grid<>(Player.class, false);

    public LeaderBoardView(LeaderBoardService leaderBoardService) {
        this.leaderBoardService = leaderBoardService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Leader Board"));
        configureGrid();
        refresh();
        add(grid);
    }

    private void configureGrid() {
        grid.addColumn(Player::getRank).setHeader("Rank").setAutoWidth(true).setSortable(true);
        grid.addColumn(Player::getFirstName).setHeader("First Name").setAutoWidth(true).setSortable(true);
        grid.addColumn(Player::getLastName).setHeader("Last Name").setAutoWidth(true).setSortable(true);
        grid.addColumn(player -> player.getEmail() == null ? "" : player.getEmail()).setHeader("Email").setAutoWidth(true);
        grid.addColumn(Player::getNumberOfGamesPlayed).setHeader("Games").setAutoWidth(true).setSortable(true);
        grid.setSizeFull();
    }

    private void refresh() {
        List<Player> players = leaderBoardService.loadPlayersSortedByRank();
        grid.setItems(players);
    }
}
