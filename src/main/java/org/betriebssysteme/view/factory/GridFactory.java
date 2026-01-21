package org.betriebssysteme.view.factory;

import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.pathfinding.CellState;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class GridFactory {

    public static AStarGrid build() {
        int width = 3200;
        int height = 3200;
        int cellHeight = 50;
        int cellWidth = 50;

        for (int y = 0; y < height / cellHeight; y++) {
            for (int x = 0; x < width / cellWidth; x++) {

                Rectangle rect = new Rectangle(cellWidth, cellHeight, Color.TRANSPARENT);
                rect.setStroke(Color.BLACK);
                rect.setStrokeType(StrokeType.INSIDE);

                Text label = new Text(x + "," + y);
                label.setFill(Color.RED);
                label.setStyle("-fx-font-size: 10px;");
                label.setMouseTransparent(true);

                // Text leicht nach innen verschieben
                label.setTranslateX(4);
                label.setTranslateY(12);

                Group view = new Group(rect, label);
                view.setMouseTransparent(true);

                entityBuilder()
                        .at(x * cellWidth, y * cellHeight)
                        .view(view)
                        .buildAndAttach();
            }
        }

        return new AStarGrid(width / cellWidth, height / cellHeight);

    }

    private static void staticallyBlockCells(AStarGrid grid) {
        Point2D[] cells = {new Point2D(10, 10)};

        for (Point2D cell : cells) {
            grid.get((int) cell.getX(), (int) cell.getY()).setState(CellState.NOT_WALKABLE);
        }


    }


}
