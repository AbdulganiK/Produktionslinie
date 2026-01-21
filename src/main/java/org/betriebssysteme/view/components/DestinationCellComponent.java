package org.betriebssysteme.view.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class DestinationCellComponent extends Component {
    private Point2D destinationCell;

    public DestinationCellComponent(int x, int y) {
        this.setDestinationCell(new Point2D(x, y));
    }

    public void setDestinationCell(Point2D destinationCell) {
        this.destinationCell = destinationCell;
    }

    public Point2D getDestinationCell() {
        return destinationCell;
    }
}
