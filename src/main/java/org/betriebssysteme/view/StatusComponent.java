package org.betriebssysteme.view;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class StatusComponent extends Component {
    Rectangle rectangle;
    Circle redLight;
    Circle greenLight;
    Circle orangeLight;

    @Override
    public void onAdded() {
        this.rectangle = new Rectangle(50, 20, Color.BLACK);
        this.rectangle.setFill(Color.TRANSPARENT);
        this.rectangle.setStroke(Color.BLACK);
        this.rectangle.setStrokeWidth(2);
        this.rectangle.setTranslateY(-20);
        this.rectangle.setTranslateX(50);
        entity.getViewComponent().addChild(this.rectangle);
    }
}
