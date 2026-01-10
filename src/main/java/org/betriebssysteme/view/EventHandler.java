package org.betriebssysteme.view;

import com.almasb.fxgl.entity.Entity;

import java.util.List;

public class EventHandler {

    public static void handleMenuCLick(javafx.scene.input.MouseEvent e, Entity clickedEntity) {
        List<Entity> entities = clickedEntity.getWorld().getEntitiesByComponent(StatusComponent.class);

        MenuComponent clickedMenu = clickedEntity.getComponent(MenuComponent.class);

        // Zustand NACH dem Klick bestimmen (toggle)
        boolean willOpen = !clickedMenu.getVisibility();

        for (Entity entity : entities) {

            MenuComponent menu = entity.getComponent(MenuComponent.class);

            // ursprünglichen zIndex einmal merken
            int currentZ = entity.getViewComponent().getZIndex();
            menu.ensureBaseZIndex(currentZ);

            if (entity == clickedEntity) {

                if (willOpen) {
                    // Menü wird geöffnet → Entity nach ganz vorne
                    menu.setVisibility(true);
                } else {
                    // Menü wird geschlossen → ursprünglichen zIndex wiederherstellen
                    entity.getViewComponent().setZIndex(menu.getBaseZIndex());
                    menu.setVisibility(false);
                }

            } else {
                // andere Entities: Menü schließen und zIndex zurücksetzen
                if (menu.getVisibility()) {
                    menu.setVisibility(false);
                }

                entity.getViewComponent().setZIndex(menu.getBaseZIndex());
            }
        }
    }

}
