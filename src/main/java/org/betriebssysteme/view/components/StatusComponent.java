package org.betriebssysteme.view.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.betriebssysteme.model.status.StatusTyp;

public class StatusComponent extends Component {



    private double offSetX = 0;
    private double offSetY = 0;

    private Rectangle rectangle;
    private Circle firstLight;
    private Circle thirdLight;

    public StatusComponent(double offSetX, double offSetY) {
        this.offSetX = offSetX;
        this.offSetY = offSetY;
    }

    public StatusComponent() {
        this.offSetX = 0;
        this.offSetY = 0;
    }

    @Override
    public void onAdded() {

        double panelWidth = 40;
        double panelHeight = 30;


        rectangle = new Rectangle(panelWidth, panelHeight);
        rectangle.setArcWidth(4);
        rectangle.setArcHeight(4);
        rectangle.setFill(Color.rgb(25, 25, 30, 0.95));
        rectangle.setStroke(Color.rgb(50, 50, 60));
        rectangle.setStrokeWidth(1.5);


        double r = 4;    // Radius der LEDs
        double centerY = panelHeight / 2.0;

        firstLight = new Circle(r, Color.BLACK);
        thirdLight = new Circle(r, Color.BLACK);

        // Positionen innerhalb des Panels
        firstLight.setTranslateX(8);
        firstLight.setTranslateY(centerY);


        thirdLight.setTranslateX(panelWidth - 8);
        thirdLight.setTranslateY(centerY);

        Group panel = new Group(rectangle, firstLight, thirdLight);

        double machineWidth = entity.getBoundingBoxComponent().getWidth();
        panel.setTranslateX((machineWidth - panelWidth) + offSetX);
        panel.setTranslateY(-18 + offSetY);   // etwas über der Maschine

        entity.getViewComponent().addChild(panel);
    }


    public void setColorFirstLamp(boolean isRunning) {
        if (isRunning) {
            this.firstLight.setFill(Color.GREEN);
        } else {
            this.firstLight.setFill(Color.RED);
        }
    }

    public void setColorFirstLamp(Color color) {
        this.firstLight.setFill(color);
    }

    public void setColorSecondLamp(StatusTyp info) {
       if (info == StatusTyp.INFO) {
           this.thirdLight.setFill(Color.GREEN);
       } else if(info == StatusTyp.CRITICAL) {
           this.thirdLight.setFill(Color.ORANGE);
       } else {
           this.thirdLight.setFill(Color.RED);
       }
    }



}
