package org.betriebssysteme.view;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.ScrollEvent;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;

public class ProductionLineApp extends GameApplication {

    Zoom zoom = new Zoom();


    @Override
    protected void initInput() {
        this.zoom.initResetZoom();
        this.zoom.initZoomToMouse();
    }



    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("Produktionslinie");
        gameSettings.setDeveloperMenuEnabled(true);
        gameSettings.setWidth(1600);
        gameSettings.setHeight(900);

    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().setGravity(0, 0);
    }

    @Override
    protected void initGame() {
        // hinzufuegen der Entity Fabrik
        getGameWorld().addEntityFactory(new EntityProductionLineFactory());

        Entity machine = FXGL.spawn(EntityNames.MACHINE, 100, 300);

        Entity machine2 = FXGL.spawn(EntityNames.MACHINE, 400, 300);

    }




    public static void main(String[] args) {
        launch(args);
    }
}
