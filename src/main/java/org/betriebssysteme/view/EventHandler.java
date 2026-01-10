package org.betriebssysteme.view;

import com.almasb.fxgl.entity.Entity;

import java.util.List;

public class EventHandler {

    public static void handleMenuCLick(javafx.scene.input.MouseEvent e, Entity clickedEntity) {
        List<Entity> entities= clickedEntity.getWorld().getEntitiesByComponent(StatusComponent.class);
        MenuComponent menu;
        MenuComponent clickedMenu = clickedEntity.getComponent(MenuComponent.class);
        clickedEntity.getViewComponent().setZIndex(100);
        for (Entity entity : entities) {
            menu = entity.getComponent(MenuComponent.class);
            if (menu != clickedMenu) {
                entity.getViewComponent().setZIndex(50);
                menu.setVisibility(false);
            }
        }
        clickedMenu.setVisibility(!clickedMenu.getVisibility());
    }
}
