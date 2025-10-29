package com.netstock.chessadmin.view;

import com.netstock.chessadmin.enums.MatchOutcome;
import com.netstock.chessadmin.dto.MatchDTO;
import com.netstock.chessadmin.dto.MatchPlayerDTO;
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
@Route(value = "/matches", layout = MainLayout.class)
public class MatchView extends VerticalLayout {

    private final MatchService matchService;
    private final Grid<MatchDTO> grid = new Grid<>(MatchDTO.class);
    private final Select<MatchPlayerDTO> playerOne;
    private final Select<MatchPlayerDTO> playerTwo;
    private final Select<MatchOutcome> outCome;
    private final Button save = new Button("Save");
    private final Button clear = new Button("Clear");
    private final Button delete = new Button("Delete");
    private final Binder<MatchDTO> binder;

    public MatchView(MatchService matchService) {
        this.binder = new Binder<>(MatchDTO.class);
        this.playerOne = getPlayerSelectInput("Player One");
        this.playerTwo = getPlayerSelectInput("Player Two");
        this.outCome = getOutcomeSelectInput("Outcome");
        this.matchService = matchService;
        grid.setColumns("playerOne", "playerTwo", "outcome");
        grid.setItems(matchService.getAllMatches());
        FormLayout form = new FormLayout(playerOne, playerTwo, outCome);
        add(grid, form);
        initButtons();
        setSizeFull();
        initMatchUpdate();
        populateDropDowns();
    }

    @NotNull
    private Select<MatchPlayerDTO> getPlayerSelectInput(String name) {
        Select<MatchPlayerDTO> playerSelect = getNewSelectField(name, MatchDTO::getPlayerOne, MatchDTO::setPlayerOne);
        playerSelect.setItemLabelGenerator(matchPlayerDTO -> matchPlayerDTO.getFirstName() + " "
                + matchPlayerDTO.getLastName());
        return playerSelect;
    }

    @NotNull
    private Select<MatchOutcome> getOutcomeSelectInput(String name) {
        Select<MatchOutcome> outcomeSelect = getNewSelectField(name, MatchDTO::getOutcome, MatchDTO::setOutcome);
        outcomeSelect.setItemLabelGenerator(outcome -> outcome.getOutcome());
        return outcomeSelect;
    }

    private void populateDropDowns() {
        List<MatchPlayerDTO> matchPlayerDTOList = matchService.getMatchPlayers();
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
            resetView();
        }
    }

    private void resetView() {
        playerOne.clear();
        playerTwo.clear();
        outCome.clear();
        binder.setBean(new MatchDTO());
    }

    private void deleteSelected() {
    }

    private void initMatchUpdate() {
    }
}
