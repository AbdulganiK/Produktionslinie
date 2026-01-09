package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MachineComponent extends Component {
    private AnimatedTexture texture;
    private AnimationChannel turningOffAnim, turningOnAnim, productionWhileTakingItemAnim, productionWithoutTakingItemAnim, offAnim;

    public MachineComponent() {
        this.offAnim = new AnimationChannel(FXGL.image("Turning_Off_Animation.png"), 7, 64, 64, Duration.seconds(2), 6, 6);
        this.turningOffAnim = new AnimationChannel(FXGL.image("Turning_Off_Animation.png"), 7, 64, 64, Duration.seconds(2), 0, 6);
        this.turningOnAnim = new AnimationChannel(FXGL.image("Turning_On_Animation.png"), 7, 64, 64, Duration.seconds(2), 0, 6);
        this.productionWhileTakingItemAnim = new AnimationChannel(FXGL.image("Production_While_Taking_Items_Animation.png"), 7, 64, 64, Duration.seconds(2), 0, 6);
        this.productionWithoutTakingItemAnim = new AnimationChannel(FXGL.image("Production_Without_Taking_Items_Animation.png"), 7, 64, 64, Duration.seconds(2), 0, 6);
        this.texture = new AnimatedTexture(offAnim);
    }

    public void handleMachineClick(javafx.scene.input.MouseEvent e) {
        List<Entity> entities= entity.getWorld().getEntitiesByType(EntityType.MACHINE);
        MenuComponent menu;
        MenuComponent clickedMenu = entity.getComponent(MenuComponent.class);
        entity.getViewComponent().setZIndex(100);
        for (Entity entity : entities) {
            menu = entity.getComponent(MenuComponent.class);
            if (menu != clickedMenu) {
                entity.getViewComponent().setZIndex(-1);
                menu.setVisibility(false);
            }
        }
        clickedMenu.setVisibility(!clickedMenu.getVisibility());
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(this.texture);
        this.texture.setOnMouseClicked(this::handleMachineClick);
        texture.loopAnimationChannel(this.productionWithoutTakingItemAnim);
    }



}
