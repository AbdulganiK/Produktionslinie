package org.betriebssysteme.view.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CentralPlatformComponent extends Component {

    private static final double PAD_WIDTH   = 420;
    private static final double DECK_HEIGHT     = 40;
    private static final double BUILDING_WIDTH  = 280;
    private static final double BUILDING_HEIGHT = 90;

    @Override
    public void onAdded() {
        Group root = new Group();

        Rectangle deck = createDeck();
        Rectangle deckEdge = createDeckEdge(deck);

        Rectangle building = createBuilding(deck);
        Rectangle base = createBuildingBase(building);
        Rectangle roof = createRoof(building);

        DoorParts doorParts = createDoor(building);
        Text title = createTitle(building);

        RailParts railParts = createRails(deck);


        root.getChildren().addAll(
                deck, deckEdge,
                building, base, roof,
                doorParts.door, doorParts.doorStep, doorParts.doorLight, doorParts.doorLabel, doorParts.doorHandle,
                title,
                railParts.leftPost, railParts.rightPost, railParts.midPost, railParts.frontRail
                // , binParts.bin1, binParts.bin2
        );

        entity.getViewComponent().addChild(root);
    }


    private Rectangle createDeck() {
        Rectangle deck = new Rectangle(PAD_WIDTH * 0.9, DECK_HEIGHT);
        deck.setFill(Color.rgb(55, 60, 75));
        deck.setStroke(Color.rgb(30, 35, 45));
        deck.setStrokeWidth(2);
        deck.setTranslateX((PAD_WIDTH - deck.getWidth()) / 2);
        return deck;
    }

    private Rectangle createDeckEdge(Rectangle deck) {
        Rectangle deckEdge = new Rectangle(deck.getWidth(), 10);
        deckEdge.setFill(Color.rgb(45, 50, 65));
        deckEdge.setTranslateX(deck.getTranslateX());
        deckEdge.setTranslateY(deck.getTranslateY() + DECK_HEIGHT - 2);
        return deckEdge;
    }


    private Rectangle createBuilding(Rectangle deck) {
        Rectangle building = new Rectangle(BUILDING_WIDTH, BUILDING_HEIGHT);
        building.setFill(Color.rgb(215, 215, 220));
        building.setStroke(Color.rgb(150, 150, 160));
        building.setStrokeWidth(2);
        building.setTranslateX(deck.getTranslateX() + (deck.getWidth() - BUILDING_WIDTH) / 2);
        building.setTranslateY(deck.getTranslateY() - BUILDING_HEIGHT + 8);
        return building;
    }

    private Rectangle createBuildingBase(Rectangle building) {
        Rectangle base = new Rectangle(BUILDING_WIDTH, 24);
        base.setFill(Color.rgb(70, 75, 95));
        base.setTranslateX(building.getTranslateX());
        base.setTranslateY(building.getTranslateY() + BUILDING_HEIGHT - base.getHeight());
        return base;
    }

    private Rectangle createRoof(Rectangle building) {
        Rectangle roof = new Rectangle(BUILDING_WIDTH + 24, 16);
        roof.setFill(Color.rgb(50, 55, 70));
        roof.setStroke(Color.rgb(30, 35, 45));
        roof.setStrokeWidth(2);
        roof.setTranslateX(building.getTranslateX() - 12);
        roof.setTranslateY(building.getTranslateY() - 14);
        return roof;
    }


    private DoorParts createDoor(Rectangle building) {
        double doorWidth = 44;
        double doorHeight = 70;

        Rectangle door = new Rectangle(doorWidth, doorHeight);
        door.setFill(Color.rgb(45, 50, 65));
        door.setStroke(Color.rgb(20, 25, 35));
        door.setStrokeWidth(3);
        door.setTranslateX(building.getTranslateX() + BUILDING_WIDTH * 0.58);
        door.setTranslateY(building.getTranslateY() + BUILDING_HEIGHT - doorHeight - 6);

        Rectangle doorStep = new Rectangle(doorWidth + 12, 6);
        doorStep.setFill(Color.rgb(170, 170, 180));
        doorStep.setTranslateX(door.getTranslateX() - 6);
        doorStep.setTranslateY(door.getTranslateY() + doorHeight + 1);

        Rectangle doorLight = new Rectangle(18, 10);
        doorLight.setArcWidth(4);
        doorLight.setArcHeight(4);
        doorLight.setFill(Color.rgb(255, 230, 120));
        doorLight.setStroke(Color.rgb(180, 160, 80));
        doorLight.setTranslateX(door.getTranslateX() + doorWidth / 2.0 - 9);
        doorLight.setTranslateY(door.getTranslateY() - 18);

        Rectangle doorLabel = new Rectangle(12, 18);
        doorLabel.setFill(Color.rgb(240, 205, 70));
        doorLabel.setTranslateX(door.getTranslateX() + doorWidth - 14);
        doorLabel.setTranslateY(door.getTranslateY() + doorHeight / 3.0);

        Rectangle doorHandle = new Rectangle(6, 10);
        doorHandle.setFill(Color.rgb(230, 190, 60));
        doorHandle.setTranslateX(door.getTranslateX() + doorWidth - 12);
        doorHandle.setTranslateY(door.getTranslateY() + doorHeight / 2.0 - 5);

        return new DoorParts(door, doorStep, doorLight, doorLabel, doorHandle);
    }

    private record DoorParts(Rectangle door, Rectangle doorStep, Rectangle doorLight, Rectangle doorLabel,
                             Rectangle doorHandle) {
    }


    private Text createTitle(Rectangle building) {
        Text title = new Text("ZENTRALE");
        title.setFill(Color.rgb(245, 210, 40));
        title.setStrokeWidth(2);
        title.setFont(Font.font("Consolas", 26));
        title.setTranslateX(building.getTranslateX() + BUILDING_WIDTH * 0.28);
        title.setTranslateY(building.getTranslateY() - 18);
        return title;
    }


    private RailParts createRails(Rectangle deck) {
        double railY = deck.getTranslateY() + 5;

        Line leftPost = new Line(deck.getTranslateX() + 20, railY, deck.getTranslateX() + 20, railY - 28);
        leftPost.setStroke(Color.rgb(245, 205, 70));
        leftPost.setStrokeWidth(4);

        Line rightPost = new Line(deck.getTranslateX() + deck.getWidth() - 20, railY,
                deck.getTranslateX() + deck.getWidth() - 20, railY - 28);
        rightPost.setStroke(leftPost.getStroke());
        rightPost.setStrokeWidth(4);

        Line frontRail = new Line(leftPost.getStartX(), leftPost.getEndY(), rightPost.getStartX(), rightPost.getEndY());
        frontRail.setStroke(leftPost.getStroke());
        frontRail.setStrokeWidth(4);

        Line midPost = new Line(deck.getTranslateX() + deck.getWidth() / 2.0, railY,
                deck.getTranslateX() + deck.getWidth() / 2.0, railY - 28);
        midPost.setStroke(leftPost.getStroke());
        midPost.setStrokeWidth(4);

        return new RailParts(leftPost, rightPost, midPost, frontRail);
    }

    private record RailParts(Line leftPost, Line rightPost, Line midPost, Line frontRail) {
    }


}
