package org.betriebssysteme.view;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.betriebssysteme.utility.EventHandler;

public class CentralPlatformComponent extends Component {

    private static final double PAD_WIDTH   = 420;
    private static final double PAD_HEIGHT  = 160;

    private static final double DECK_HEIGHT     = 40;
    private static final double BUILDING_WIDTH  = 280;
    private static final double BUILDING_HEIGHT = 90;

    @Override
    public void onAdded() {

        Group root = new Group();

        Rectangle deck = new Rectangle(PAD_WIDTH * 0.9, DECK_HEIGHT);
        deck.setFill(Color.rgb(55, 60, 75));
        deck.setStroke(Color.rgb(30, 35, 45));
        deck.setStrokeWidth(2);
        deck.setTranslateX((PAD_WIDTH - deck.getWidth()) / 2);

        // dünnere Kante direkt unter dem Deck
        Rectangle deckEdge = new Rectangle(deck.getWidth(), 10);
        deckEdge.setFill(Color.rgb(45, 50, 65));
        deckEdge.setTranslateX(deck.getTranslateX());
        deckEdge.setTranslateY(deck.getTranslateY() + DECK_HEIGHT - 2);

        // Gebäude auf dem Podest
        Rectangle building = new Rectangle(BUILDING_WIDTH, BUILDING_HEIGHT);
        building.setFill(Color.rgb(215, 215, 220));
        building.setStroke(Color.rgb(150, 150, 160));
        building.setStrokeWidth(2);
        building.setTranslateX(deck.getTranslateX() + (deck.getWidth() - BUILDING_WIDTH) / 2);
        building.setTranslateY(deck.getTranslateY() - BUILDING_HEIGHT + 8);

        // dunkler Sockel am Gebäude
        Rectangle base = new Rectangle(BUILDING_WIDTH, 24);
        base.setFill(Color.rgb(70, 75, 95));
        base.setTranslateX(building.getTranslateX());
        base.setTranslateY(building.getTranslateY() + BUILDING_HEIGHT - base.getHeight());

        // Dach
        Rectangle roof = new Rectangle(BUILDING_WIDTH + 24, 16);
        roof.setFill(Color.rgb(50, 55, 70));
        roof.setStroke(Color.rgb(30, 35, 45));
        roof.setStrokeWidth(2);
        roof.setTranslateX(building.getTranslateX() - 12);
        roof.setTranslateY(building.getTranslateY() - 14);

        // Tür vorne
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


        Text title = new Text("ZENTRALE");
        title.setFill(Color.rgb(245, 210, 40));
        title.setStrokeWidth(2);
        title.setFont(Font.font("Consolas", 26));
        title.setTranslateX(building.getTranslateX() + BUILDING_WIDTH * 0.28);
        title.setTranslateY(building.getTranslateY() - 18);


        double railY = deck.getTranslateY() + 5;

        Line leftPost = new Line(deck.getTranslateX() + 20, railY, deck.getTranslateX() + 20, railY - 28);
        leftPost.setStroke(Color.rgb(245, 205, 70));
        leftPost.setStrokeWidth(4);

        Line rightPost = new Line(deck.getTranslateX() + deck.getWidth() - 20, railY, deck.getTranslateX() + deck.getWidth() - 20, railY - 28);
        rightPost.setStroke(leftPost.getStroke());
        rightPost.setStrokeWidth(4);

        Line frontRail = new Line(
                leftPost.getStartX(),
                leftPost.getEndY(),
                rightPost.getStartX(),
                rightPost.getEndY()
        );
        frontRail.setStroke(leftPost.getStroke());
        frontRail.setStrokeWidth(4);

        Line midPost = new Line(
                deck.getTranslateX() + deck.getWidth() / 2.0,
                railY,
                deck.getTranslateX() + deck.getWidth() / 2.0,
                railY - 28
        );
        midPost.setStroke(leftPost.getStroke());
        midPost.setStrokeWidth(4);

        Rectangle bin1 = new Rectangle(14, 26);
        bin1.setArcWidth(6);
        bin1.setArcHeight(6);
        bin1.setFill(Color.rgb(245, 205, 70));
        bin1.setStroke(Color.rgb(180, 150, 50));
        bin1.setTranslateX(door.getTranslateX() + doorWidth + 16);
        bin1.setTranslateY(door.getTranslateY() + doorHeight - 20);

        Rectangle bin2 = new Rectangle(18, 22);
        bin2.setArcWidth(8);
        bin2.setArcHeight(8);
        bin2.setFill(Color.rgb(245, 205, 70));
        bin2.setStroke(Color.rgb(180, 150, 50));
        bin2.setTranslateX(bin1.getTranslateX() + 18);
        bin2.setTranslateY(bin1.getTranslateY() - 8);

        root.getChildren().addAll(
                deck, deckEdge,
                building, base, roof,
                door, doorStep, doorLight, doorLabel, doorHandle,
                title,
                leftPost, rightPost, midPost, frontRail
        );

        entity.getViewComponent().addChild(root);

        root.setOnMouseClicked(this::handleCentralClick);
    }

    public void handleCentralClick(MouseEvent e) {
        EventHandler.handleMenuCLick(e, entity);
    }
}
