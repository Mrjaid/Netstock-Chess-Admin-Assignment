package com.netstock.chessadmin.view;

import com.netstock.chessadmin.dto.PlayerDTO;
import com.netstock.chessadmin.service.PlayerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import com.vaadin.flow.data.binder.Setter;

import java.time.LocalDate;
import java.util.Objects;

@PageTitle("Players")
@Route(value = "", layout = MainLayout.class)
public class PlayerView extends VerticalLayout {

    private final PlayerService playerService;
    private final Grid<PlayerDTO> grid = new Grid<>(PlayerDTO.class);
    private final TextField firstName;
    private final TextField lastName;
    private final TextField email;
    private final DatePicker dateOfBirth;
    private final Button save = new Button("Save");
    private final Button clear = new Button("Clear");
    private final Button delete = new Button("Delete");
    private final Binder<PlayerDTO> binder;
    private Long selectedId;

    public PlayerView(PlayerService personService) {
        this.binder = new Binder<>(PlayerDTO.class);
        this.firstName = getNewTextField("First Name", PlayerDTO::getFirstName, PlayerDTO::setFirstName);
        this.lastName = getNewTextField("Last Name", PlayerDTO::getLastName, PlayerDTO::setLastName);
        this.email = getNewTextField("Email", PlayerDTO::getEmail, PlayerDTO::setEmail);
        this.dateOfBirth = getDateField("Birthday", PlayerDTO::getDateOfBirth, PlayerDTO::setDateOfBirth);
        this.playerService = personService;
        grid.setColumns("id", "firstName", "lastName", "email", "dateOfBirth");
        grid.setItems(personService.findAll());
        FormLayout form = new FormLayout(firstName, lastName, email, dateOfBirth);
        add(grid, form);
        initButtons();
        setSizeFull();
        initPlayerUpdate();
    }

    private void initButtons() {
        save.addClickListener(e -> savePerson());
        delete.addClickListener(e -> deleteSelected());
        clear.addClickListener(e -> resetView());
        HorizontalLayout buttonLayout = new HorizontalLayout(save, delete, clear);
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);
        add(buttonLayout);
    }

    @NotNull
    private TextField getNewTextField(String inputName, ValueProvider<PlayerDTO, String> getter, Setter<PlayerDTO, String> setter) {
        TextField field = new TextField(inputName);
        field.setRequiredIndicatorVisible(true);
        field.setRequired(true);
        field.setErrorMessage(inputName + " is required");
        binder.forField(field)
                .asRequired(inputName + " is required")
                .bind(getter, setter);
        return field;
    }

    @NotNull
    private DatePicker getDateField(String inputName, ValueProvider<PlayerDTO, LocalDate> getter, Setter<PlayerDTO, LocalDate> setter) {
        DatePicker field = new DatePicker();
        field.setRequiredIndicatorVisible(true);
        field.setRequired(true);
        field.setErrorMessage(inputName + " is required");
        binder.forField(field)
                .asRequired(inputName + " is required")
                .bind(getter, setter);
        return field;
    }

    private void savePerson() {
        if (binder.validate().isOk()) {
            PlayerDTO person = getPlayerFromView();
            if (Objects.nonNull(selectedId)) {
                person.setId(selectedId);
            }
            playerService.save(person);
            grid.setItems(playerService.findAll());
            resetView();
        }
    }

    private void resetView() {
        firstName.clear();
        lastName.clear();
        email.clear();
        dateOfBirth.clear();
        binder.setBean(new PlayerDTO());
        selectedId = null;
    }

    private PlayerDTO getPlayerFromView() {
        return PlayerDTO.builder()
                .firstName(firstName.getValue())
                .lastName(lastName.getValue())
                .email(email.getValue())
                .dateOfBirth(dateOfBirth.getValue())
                .build();
    }

    private void deleteSelected() {
        PlayerDTO selected = grid.asSingleSelect().getValue();
        if (selected != null) {
            playerService.delete(selected.getId());
            grid.setItems(playerService.findAll());
            resetView();
        }
    }

    private void initPlayerUpdate() {
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.asSingleSelect().addValueChangeListener(event -> {
            PlayerDTO selected = event.getValue();
            if (selected != null) {
                firstName.setValue(selected.getFirstName());
                lastName.setValue(selected.getLastName());
                email.setValue(selected.getEmail());
                dateOfBirth.setValue(selected.getDateOfBirth());
                selectedId = selected.getId();
            }
        });
    }
}
