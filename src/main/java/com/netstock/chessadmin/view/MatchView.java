package com.netstock.chessadmin.view;

import com.netstock.chessadmin.dto.PlayerDTO;
import com.netstock.chessadmin.enums.MatchOutcome;
import com.netstock.chessadmin.dto.MatchDTO;
import com.netstock.chessadmin.service.MatchService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@PageTitle("Matches")
@Route(value = "match", layout = MainLayout.class)
public class MatchView extends VerticalLayout {

    private final MatchService matchService;
    private final Select<PlayerDTO> playerOne;
    private final Select<PlayerDTO> playerTwo;
    private final Select<MatchOutcome> outCome;
    private final Button save = new Button("Save");
    private final Button clear = new Button("Clear");
    private final Button delete = new Button("Delete");
    private final Binder<MatchDTO> binder;
    private final Grid<MatchDTO> grid = getConfigedGrid();

    public MatchView(MatchService matchService) {
        this.binder = new Binder<>(MatchDTO.class);
        this.playerOne = getPlayerSelectInput("Player One", MatchDTO::getPlayerOne, MatchDTO::setPlayerOne);
        this.playerTwo = getPlayerSelectInput("Player Two", MatchDTO::getPlayerTwo, MatchDTO::setPlayerTwo);
        this.outCome = getOutcomeSelectInput();
        this.matchService = matchService;
        grid.setItems(matchService.getAllMatches());
        FormLayout form = new FormLayout(playerOne, playerTwo, outCome);
        add(grid, form);
        initButtons();
        setSizeFull();
        populateDropDowns();
    }

    @NotNull
    private Grid<MatchDTO> getConfigedGrid() {
        Grid<MatchDTO> grid = new Grid<>(MatchDTO.class, false);
        addPlayerOneToGrid(grid);
        addPlayerTwoToGrid(grid);
        addOutcomeToGrid(grid);
        return grid;
    }

    private void addOutcomeToGrid(Grid<MatchDTO> grid) {
        grid.addColumn(this::getOutcomeName).setHeader("Outcome").setAutoWidth(true);
    }

    private void addPlayerTwoToGrid(Grid<MatchDTO> grid) {
        grid.addColumn(match ->
                getPlayerName(match.getPlayerTwo())).setHeader("Player Two").setAutoWidth(true);
    }

    private void addPlayerOneToGrid(Grid<MatchDTO> grid) {
        grid.addColumn(match ->
                getPlayerName(match.getPlayerOne())).setHeader("Player One").setAutoWidth(true);
    }

    private String getOutcomeName(@NotNull MatchDTO matchDTO) {
       return matchDTO.getOutcome().getOutcome();
    }

    private String getPlayerName(PlayerDTO player) {
        if (null == player) {
            return " *** Deleted Player **";
        }
        String playerName = trimNull(player.getFirstName()) + " " + trimNull(player.getLastName());
        playerName += " ( "+ player.getRank()+" )";
        return playerName ;
    }

    private String trimNull(String value) {
        return value == null ? "" : value;
    }

    @NotNull
    private Select<PlayerDTO> getPlayerSelectInput(String name, ValueProvider<MatchDTO, PlayerDTO> getter, Setter<MatchDTO, PlayerDTO> setter) {
        Select<PlayerDTO> playerSelect = getNewSelectField(name, getter, setter);
        playerSelect.setItemLabelGenerator(matchPlayerDTO -> matchPlayerDTO.getFirstName() + " "
                + matchPlayerDTO.getLastName());
        return playerSelect;
    }

    @NotNull
    private Select<MatchOutcome> getOutcomeSelectInput() {
        Select<MatchOutcome> outcomeSelect = getNewSelectField("Outcome", MatchDTO::getOutcome, MatchDTO::setOutcome);
        outcomeSelect.setItemLabelGenerator(MatchOutcome::getOutcome);
        return outcomeSelect;
    }

    private void populateDropDowns() {
        List<PlayerDTO> matchPlayerDTOList = matchService.getMatchPlayers();
        this.playerOne.setItems(matchPlayerDTOList);
        this.playerTwo.setItems(matchPlayerDTOList);
        this.outCome.setItems(MatchOutcome.values());
    }

    private void initButtons() {
        save.addClickListener(e -> saveMatch());
        delete.addClickListener(e -> deleteSelected());
        clear.addClickListener(e -> resetView());
        HorizontalLayout buttonLayout = new HorizontalLayout(save, delete, clear);
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);
        add(buttonLayout);
    }

    @NotNull
    private <T> Select<T> getNewSelectField(String inputName, ValueProvider<MatchDTO, T> getter, Setter<MatchDTO, T> setter) {
        Select<T> field = new Select<>();
        field.setRequiredIndicatorVisible(true);
        field.setLabel(inputName);
        field.setErrorMessage(inputName + " is required");
        binder.forField(field)
                .asRequired(inputName + " is required")
                .bind(getter, setter);
        return field;
    }

    private void saveMatch() {
        if (binder.validate().isOk()) {
            matchService.saveMatch(getMatchFromView());
            resetView();
            grid.setItems(matchService.getAllMatches());
        }
    }

    private MatchDTO getMatchFromView() {
        return MatchDTO.builder()
                .playerOne(playerOne.getValue())
                .playerTwo(playerTwo.getValue())
                .outcome(outCome.getValue())
                .build();
    }

    private void resetView() {
        playerOne.clear();
        playerTwo.clear();
        outCome.clear();
        binder.setBean(new MatchDTO());
    }

    private void deleteSelected() {
        MatchDTO selected = grid.asSingleSelect().getValue();
        if (selected != null && selected.getId() != null) {
            matchService.deleteMatch(selected.getId());
            grid.setItems(matchService.getAllMatches());
            resetView();
        } else if (selected != null) {
            resetView();
        }
    }
}
