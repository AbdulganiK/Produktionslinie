package org.betriebssysteme.utility;

import com.almasb.fxgl.entity.Entity;
import org.betriebssysteme.view.components.MenuComponent;
import org.betriebssysteme.view.components.StatusComponent;

import java.util.List;

public class EventHandler {

    public static void handleMenuCLick(javafx.scene.input.MouseEvent e, Entity clickedEntity) {
        List<Entity> entities = clickedEntity.getWorld().getEntitiesByComponent(MenuComponent.class);

        MenuComponent clickedMenu = clickedEntity.getComponent(MenuComponent.class);

        boolean willOpen = !clickedMenu.getVisibility();

        for (Entity entity : entities) {

            MenuComponent menu = entity.getComponent(MenuComponent.class);

            int currentZ = entity.getViewComponent().getZIndex();
            menu.ensureBaseZIndex(currentZ);

            if (entity == clickedEntity) {

                if (willOpen) {
                    // Menü wird geöffnet
                    menu.setVisibility(true);
                } else {
                    // Menü wird geschlossen
                    entity.getViewComponent().setZIndex(menu.getBaseZIndex());
                    menu.setVisibility(false);
                }

            } else {
                // andere Entities
                if (menu.getVisibility()) {
                    menu.setVisibility(false);
                }

                entity.getViewComponent().setZIndex(menu.getBaseZIndex());
            }
        }
    }

}
