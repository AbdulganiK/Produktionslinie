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

        AStarGrid grid = new AStarGrid(width / cellWidth, height / cellHeight);
        staticallyBlockCells(grid);
        return grid;

    }

    private static void staticallyBlockCells(AStarGrid grid) {
        Point2D[] cells = {
                new Point2D(15, 11),
                new Point2D(15, 12),
                new Point2D(15, 13),
                new Point2D(15, 14),
                new Point2D(15, 15),
                new Point2D(15, 16),
                new Point2D(15, 17),
                new Point2D(15, 18),
                new Point2D(15, 19),
                new Point2D(15, 20),
                new Point2D(15, 21),


                new Point2D(18, 12),
                new Point2D(18, 13),
                new Point2D(18, 14),
                new Point2D(18, 15),
                new Point2D(18, 16),
                new Point2D(18, 17),
                new Point2D(18, 18),
                new Point2D(18, 19),
                new Point2D(18, 20),
                new Point2D(18, 21),
                new Point2D(18, 22),

                new Point2D(19, 12),
                new Point2D(19, 13),
                new Point2D(19, 14),
                new Point2D(19, 15),
                new Point2D(19, 16),
                new Point2D(19, 17),
                new Point2D(19, 18),
                new Point2D(19, 19),
                new Point2D(19, 20),
                new Point2D(19, 21),
                new Point2D(19, 22),

                new Point2D(6, 16),
                new Point2D(7, 16),

                new Point2D(7, 17),
                new Point2D(8, 17),
                new Point2D(9, 17),

                new Point2D(9, 18),
                new Point2D(10, 18),
                new Point2D(11, 18),

                new Point2D(11, 19),
                new Point2D(12, 19),
                new Point2D(13, 19),

                new Point2D(13, 20),
                new Point2D(14, 20),
                new Point2D(5, 16),
                new Point2D(5, 15),
                new Point2D(6, 15),
                new Point2D(7, 15),
                new Point2D(7, 14),
                new Point2D(8, 14),
                new Point2D(9, 14),
                new Point2D(9, 13),
                new Point2D(10, 13),
                new Point2D(11, 13),
                new Point2D(11, 12),
                new Point2D(12, 12),
                new Point2D(13, 12),
                new Point2D(13, 11),
                new Point2D(12, 11),
                new Point2D(12, 10),
                new Point2D(11, 10),
                new Point2D(12, 9),
                new Point2D(13, 9),
        };

        for (Point2D cell : cells) {
            grid.get((int) cell.getX(), (int) cell.getY()).setState(CellState.NOT_WALKABLE);
        }


    }


}
