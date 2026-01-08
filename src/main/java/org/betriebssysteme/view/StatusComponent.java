package org.betriebssysteme.view;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class StatusComponent extends Component {

    private Rectangle rectangle;
    private Circle redLight;
    private Circle orangeLight;
    private Circle greenLight;

    @Override
    public void onAdded() {

        // Größe des Panels (breiter, aber flach)
        double panelWidth = 46;
        double panelHeight = 14;


        rectangle = new Rectangle(panelWidth, panelHeight);
        // Runde Ecken
        rectangle.setArcWidth(4);
        rectangle.setArcHeight(4);
        // Füllfarbe und Rand
        rectangle.setFill(Color.rgb(25, 25, 30, 0.95));
        rectangle.setStroke(Color.rgb(50, 50, 60));
        rectangle.setStrokeWidth(1.5);

        // Kleine LED-Lichter innerhalb des Panels
        double r = 3;    // Radius der LEDs
        double centerY = panelHeight / 2.0;

        redLight = new Circle(r, Color.BLACK);
        orangeLight = new Circle(r, Color.BLACK);
        greenLight = new Circle(r, Color.BLACK);

        // Positionen innerhalb des Panels
        redLight.setTranslateX(8);
        redLight.setTranslateY(centerY);

        orangeLight.setTranslateX(panelWidth / 2.0);
        orangeLight.setTranslateY(centerY);

        greenLight.setTranslateX(panelWidth - 8);
        greenLight.setTranslateY(centerY);

        // Alles in eine Gruppe packen, damit wir das Panel als Ganzes verschieben können
        Group panel = new Group(rectangle, redLight, orangeLight, greenLight);


        double machineWidth = entity.getBoundingBoxComponent().getWidth();
        panel.setTranslateX((machineWidth - panelWidth) / 2.0);
        panel.setTranslateY(-18);   // etwas über der Maschine

        this.warning();
        entity.getViewComponent().addChild(panel);
    }


    private void allOff() {
        redLight.setFill(Color.BLACK);
        orangeLight.setFill(Color.BLACK);
        greenLight.setFill(Color.BLACK);
    }

    // ERROR: nur Rot leuchtet
    public void error() {
        allOff();
        redLight.setFill(Color.RED);
    }

    // RUNNING: nur Grün leuchtet
    public void running() {
        allOff();
        greenLight.setFill(Color.GREEN);
    }

    // WARNING: nur Orange leuchtet
    public void warning() {
        allOff();
        orangeLight.setFill(Color.ORANGE);
    }

    public void turnAllOff() {
        allOff();
    }

}
