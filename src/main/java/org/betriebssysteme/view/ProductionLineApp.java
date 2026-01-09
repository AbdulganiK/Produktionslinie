package org.betriebssysteme.view;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.entity.level.tiled.TiledMap;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.input.ScrollEvent;
import com.almasb.fxgl.entity.level.tiled.TiledMap;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getAssetLoader;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.*;

public class ProductionLineApp extends GameApplication {

    Zoom zoom = new Zoom();
    private Point2D lastDragPos;


    @Override
    protected void initInput() {
        this.zoom.initResetZoom();
        this.zoom.initZoomToMouse();
        this.initCameraDrag();
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


        setLevelFromMap("map.tmx");
        // hinzufuegen der Entity Fabrik
        getGameWorld().addEntityFactory(new EntityProductionLineFactory());

        Entity machine = FXGL.spawn(EntityNames.MACHINE, 100, 300);

        Entity machine2 = FXGL.spawn(EntityNames.MACHINE, 400, 300);

    }

    private void initCameraDrag() {
        var scene = FXGL.getPrimaryStage().getScene();
        Viewport vp = FXGL.getGameScene().getViewport();

        // Maus runter -> Startpunkt merken
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.MIDDLE) {   // oder PRIMARY / SECONDARY, wie du willst
                lastDragPos = new Point2D(e.getSceneX(), e.getSceneY());
            }
        });

        // Maus ziehen -> Kamera bewegen
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (e.isMiddleButtonDown() && lastDragPos != null) {

                double dxScreen = e.getSceneX() - lastDragPos.getX();
                double dyScreen = e.getSceneY() - lastDragPos.getY();

                double zoom = vp.getZoom();

                // Screen-Pixel in Weltverschiebung umrechnen
                double dxWorld = -dxScreen / zoom;
                double dyWorld = -dyScreen / zoom;

                vp.setX(vp.getX() + dxWorld);
                vp.setY(vp.getY() + dyWorld);

                lastDragPos = new Point2D(e.getSceneX(), e.getSceneY());
            }
        });

        // Maus loslassen -> Drag beenden
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getButton() == MouseButton.MIDDLE) {
                lastDragPos = null;
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
