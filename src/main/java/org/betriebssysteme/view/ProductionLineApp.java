package org.betriebssysteme.view;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import javafx.geometry.Point2D;


import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import org.betriebssysteme.utility.EntityCollisionHandler;
import org.betriebssysteme.view.extras.Camera;
import org.betriebssysteme.view.extras.Zoom;
import org.betriebssysteme.view.factory.EntityNames;
import org.betriebssysteme.view.factory.EntityProductionLineFactory;
import org.betriebssysteme.view.factory.EntityType;
import org.betriebssysteme.view.factory.GridFactory;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.*;

public class ProductionLineApp extends GameApplication {

    private Zoom zoom = new Zoom();
    private Camera camera = new Camera();
    private EntityProductionLineFactory entityFactory = new EntityProductionLineFactory();
    private Entity clerk;
    private AStarGrid grid;

    private Entity emptyPoint;


    @Override
    protected void initInput() {
        this.zoom.initResetZoom();
        this.zoom.initZoomToMouse();
        camera.initCameraDrag();
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
        getPhysicsWorld().setGravity(0, 0);
        // Collision Handling
        EntityCollisionHandler.addCollisionBetweenMachineAndEntity(getPhysicsWorld());
        EntityCollisionHandler.addCollisionBetweenItems(getPhysicsWorld());
        EntityCollisionHandler.addCollisionBetweenSupplierAndStorage(getPhysicsWorld());
        EntityCollisionHandler.addCollisionBetweenItemAndBelt(getPhysicsWorld());
    }



    @Override
    protected void initGame() {

        // Karte setzen
        setLevelFromMap("map.tmx");

        // hinzufuegen der Entity Fabrik
        getGameWorld().addEntityFactory(entityFactory);

        // Laden des Grids
        grid = GridFactory.build();

        // Laden des Scenario
        Scenario scenario = new Scenario(entityFactory);
        scenario.runFirst();
        Rectangle rect = new Rectangle(50, 50, Color.BLACK);
        rect.setStroke(Color.BLACK);
        rect.setStrokeType(StrokeType.INSIDE);

        Group view = new Group(rect);
        view.setMouseTransparent(true);


        this.emptyPoint = entityBuilder()
                .at(100 + 690, 400 + 300)
                .view(view)
                .buildAndAttach();



    }

    public Entity getEmptyPoint() {
        return emptyPoint;
    }

    public Entity spawnItemOnBelt(Entity belt) {
        Point2D center = belt.getCenter();

        double itemHalfW = 16;
        double itemHalfH = 16;

        return FXGL.spawn(
                EntityNames.ITEM,
                center.getX() - itemHalfW,
                center.getY() - itemHalfH
        );
    }

    public AStarGrid getGrid() {
        return grid;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
