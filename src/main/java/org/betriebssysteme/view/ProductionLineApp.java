package org.betriebssysteme.view;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.GameWorld;
import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;

public class ProductionLineApp extends GameApplication {

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("Produktionslinie");

    }

    @Override
    protected void initGame() {
        // hinzufuegen der Entity Fabrik
        getGameWorld().addEntityFactory(new EntityProductionLineFactory());

        // Spawnen einer Maschine
        Entity machine = FXGL.spawn(EntityNames.MACHINE);
        machine.setPosition(new Point2D(100, 10));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
