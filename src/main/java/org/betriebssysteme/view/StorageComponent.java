package org.betriebssysteme.view;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.betriebssysteme.utility.EventHandler;

public class StorageComponent extends Component {

    // Basisgrößen
    private static final double PAD_WIDTH   = 420;
    private static final double PAD_HEIGHT  = 140;

    private static final double BUILDING_WIDTH  = 400;
    private static final double BUILDING_HEIGHT = 90;

    private static final double BASE_HEIGHT = 20;   // dunkler Sockel

    @Override
    public void onAdded() {


        Group root = new Group();

        //Asphalp
        Rectangle pad = new Rectangle(PAD_WIDTH, PAD_HEIGHT);
        pad.setFill(Color.rgb(70, 70, 80));                // dunkler Asphalt
        pad.setStroke(Color.rgb(120, 120, 130));           // Rand
        pad.setStrokeWidth(2);

        // leicht nach unten versetzen
        pad.setTranslateY(40);

        // Gebäude
        Rectangle building = new Rectangle(BUILDING_WIDTH, BUILDING_HEIGHT);
        building.setFill(Color.rgb(205, 205, 210));        // helles Grau
        building.setStroke(Color.rgb(120, 120, 130));
        building.setStrokeWidth(2);
        building.setTranslateX((PAD_WIDTH - BUILDING_WIDTH) / 2);
        building.setTranslateY(0);

        // dunkler Sockel
        Rectangle base = new Rectangle(BUILDING_WIDTH, BASE_HEIGHT);
        base.setFill(Color.rgb(60, 70, 95));
        base.setTranslateX(building.getTranslateX());
        base.setTranslateY(building.getTranslateY() + BUILDING_HEIGHT - BASE_HEIGHT);

        // Dachkante
        Rectangle roof = new Rectangle(BUILDING_WIDTH, 8);
        roof.setFill(Color.rgb(180, 180, 190));
        roof.setTranslateX(building.getTranslateX());
        roof.setTranslateY(building.getTranslateY() - 8);

        // vertikale Fugen / Säulen
        Line col1 = new Line();
        col1.setStartX(building.getTranslateX() + BUILDING_WIDTH * 0.33);
        col1.setEndX(col1.getStartX());
        col1.setStartY(building.getTranslateY() + 5);
        col1.setEndY(building.getTranslateY() + BUILDING_HEIGHT - 5);
        col1.setStroke(Color.rgb(150, 150, 160));
        col1.setStrokeWidth(3);

        Line col2 = new Line();
        col2.setStartX(building.getTranslateX() + BUILDING_WIDTH * 0.66);
        col2.setEndX(col2.getStartX());
        col2.setStartY(col1.getStartY());
        col2.setEndY(col1.getEndY());
        col2.setStroke(col1.getStroke());
        col2.setStrokeWidth(3);

        // horizontale Paneel-Linien
        for (int i = 1; i <= 3; i++) {
            double y = building.getTranslateY() + i * (BUILDING_HEIGHT / 4.0);
            Line line = new Line(
                    building.getTranslateX() + 8, y,
                    building.getTranslateX() + BUILDING_WIDTH - 8, y
            );
            line.setStroke(Color.rgb(185, 185, 190));
            line.setStrokeWidth(1.2);
            root.getChildren().add(line);
        }

        // Tor links
        double gateWidth = BUILDING_WIDTH * 0.35;
        double gateHeight = BUILDING_HEIGHT * 0.75;

        Rectangle gateFrame = new Rectangle(gateWidth, gateHeight);
        gateFrame.setFill(Color.rgb(40, 40, 45));
        gateFrame.setStroke(Color.rgb(20, 20, 25));
        gateFrame.setStrokeWidth(4);
        gateFrame.setTranslateX(building.getTranslateX() + 15);
        gateFrame.setTranslateY(building.getTranslateY() + BUILDING_HEIGHT - gateHeight - 5);

        // gelbe Streifen links und rechts
        Rectangle gateLeft = new Rectangle(8, gateHeight);
        gateLeft.setFill(Color.rgb(230, 190, 60));
        gateLeft.setTranslateX(gateFrame.getTranslateX() - 8);
        gateLeft.setTranslateY(gateFrame.getTranslateY());

        Rectangle gateRight = new Rectangle(8, gateHeight);
        gateRight.setFill(Color.rgb(230, 190, 60));
        gateRight.setTranslateX(gateFrame.getTranslateX() + gateWidth);
        gateRight.setTranslateY(gateFrame.getTranslateY());

        // gelber Umriss vor dem Tor auf dem Boden
        double padYTop = pad.getTranslateY();
        Rectangle gateZone = new Rectangle(gateWidth * 0.9, 6);
        gateZone.setFill(Color.TRANSPARENT);
        gateZone.setStroke(Color.rgb(255, 220, 90));
        gateZone.setStrokeWidth(2);
        gateZone.setTranslateX(gateFrame.getTranslateX() + gateWidth * 0.05);
        gateZone.setTranslateY(padYTop + PAD_HEIGHT * 0.1);

        // seitliche Linien der Zone
        Line zoneLeft = new Line(
                gateZone.getTranslateX(),
                gateZone.getTranslateY(),
                gateZone.getTranslateX(),
                gateFrame.getTranslateY() + gateHeight
        );
        zoneLeft.setStroke(gateZone.getStroke());
        zoneLeft.setStrokeWidth(2);

        Line zoneRight = new Line(
                gateZone.getTranslateX() + gateZone.getWidth(),
                gateZone.getTranslateY(),
                gateZone.getTranslateX() + gateZone.getWidth(),
                gateFrame.getTranslateY() + gateHeight
        );
        zoneRight.setStroke(gateZone.getStroke());
        zoneRight.setStrokeWidth(2);

        // kleines Panel über dem Tor
        Rectangle gateSign = new Rectangle(gateWidth * 0.5, 14);
        gateSign.setArcWidth(6);
        gateSign.setArcHeight(6);
        gateSign.setFill(Color.rgb(40, 40, 45));
        gateSign.setStroke(Color.rgb(20, 20, 25));
        gateSign.setTranslateX(gateFrame.getTranslateX() + gateWidth * 0.25);
        gateSign.setTranslateY(building.getTranslateY() - 4);

        // Seitentür rechts
        double doorWidth = 40;
        double doorHeight = 65;

        Rectangle doorFrame = new Rectangle(doorWidth, doorHeight);
        doorFrame.setFill(Color.rgb(55, 65, 85));
        doorFrame.setStroke(Color.rgb(25, 30, 40));
        doorFrame.setStrokeWidth(3);
        doorFrame.setTranslateX(building.getTranslateX() + BUILDING_WIDTH - doorWidth - 30);
        doorFrame.setTranslateY(building.getTranslateY() + BUILDING_HEIGHT - doorHeight - 5);

        // Türschwelle
        Rectangle doorStep = new Rectangle(doorWidth + 10, 6);
        doorStep.setFill(Color.rgb(180, 180, 190));
        doorStep.setTranslateX(doorFrame.getTranslateX() - 5);
        doorStep.setTranslateY(doorFrame.getTranslateY() + doorHeight + 1);

        // Türlicht oben
        Rectangle doorLight = new Rectangle(16, 10);
        doorLight.setArcWidth(4);
        doorLight.setArcHeight(4);
        doorLight.setFill(Color.rgb(255, 230, 120));
        doorLight.setStroke(Color.rgb(180, 160, 80));
        doorLight.setTranslateX(doorFrame.getTranslateX() + doorWidth / 2.0 - 8);
        doorLight.setTranslateY(doorFrame.getTranslateY() - 18);

        Rectangle doorHandle = new Rectangle(6, 10);
        doorHandle.setFill(Color.rgb(230, 190, 60));
        doorHandle.setTranslateX(doorFrame.getTranslateX() + doorWidth - 12);
        doorHandle.setTranslateY(doorFrame.getTranslateY() + doorHeight / 2.0 - 5);

        // gelbe Säule rechts
        Rectangle pillar = new Rectangle(10, 40);
        pillar.setArcWidth(6);
        pillar.setArcHeight(6);
        pillar.setFill(Color.rgb(250, 210, 70));
        pillar.setStroke(Color.rgb(180, 150, 50));
        pillar.setTranslateX(doorFrame.getTranslateX() + doorWidth + 18);
        pillar.setTranslateY(doorFrame.getTranslateY() + doorHeight - 20);

        // Alles zur Root-Group hinzufügen
        root.getChildren().addAll(
                pad,
                building, base, roof,
                col1, col2,
                gateFrame, gateLeft, gateRight,
                gateZone, zoneLeft, zoneRight,
                gateSign,
                doorFrame, doorStep, doorLight, doorHandle, pillar
        );

        // Root-Gruppe an Entity hängen
        entity.getViewComponent().addChild(root);

        // einfache Hitbox über Gebäude + Boden
        entity.getBoundingBoxComponent().clearHitBoxes();
        entity.getBoundingBoxComponent().addHitBox(
                new com.almasb.fxgl.physics.HitBox("STORAGE",
                        new Point2D(0, 0),
                        com.almasb.fxgl.physics.BoundingShape.box(PAD_WIDTH, PAD_HEIGHT + 40))
        );

        root.setOnMouseClicked(this::handleStorageClick);
    }

    public void handleStorageClick(MouseEvent e) {
       EventHandler.handleMenuCLick(e, entity);
    }
}
