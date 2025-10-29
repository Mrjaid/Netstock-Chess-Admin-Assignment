package com.netstock.chessadmin.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    public MainLayout() {
        H1 title = new H1("Chess Admin App");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);

        DrawerToggle toggle = new DrawerToggle();
        addToNavbar(toggle, title);
        initSideMenuItems();
    }

    private void initSideMenuItems() {
        VerticalLayout menuLayout = new VerticalLayout(new Span("Menu"));
        menuLayout.setPadding(true);
        menuLayout.setSpacing(true);
        menuLayout.add(new RouterLink("Players", PlayerView.class));
        menuLayout.add(new RouterLink("Matches", MatchView.class));
        addToDrawer(menuLayout);
    }
}
