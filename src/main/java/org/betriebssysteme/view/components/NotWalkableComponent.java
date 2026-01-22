package org.betriebssysteme.view.components;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.pathfinding.CellState;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;

public class NotWalkableComponent extends Component {

    private final AStarGrid grid;
    private final int rangeX;
    private final int rangeY;
    private final int cellSize;
    private final int offsetX;
    private final int offsetY;

    public NotWalkableComponent(AStarGrid grid) {
        this(grid, 0, 0, 50, 0, 0);
    }

    public NotWalkableComponent(AStarGrid grid, int rangeX, int rangeY, int offsetX, int offsetY) {
        this(grid, rangeX, rangeY, 50, offsetX, offsetY);
    }

    public NotWalkableComponent(AStarGrid grid, int rangeX, int rangeY, int cellSize) {
        this(grid, rangeX, rangeY, cellSize, 0, 0);
    }

    // Hauptkonstruktor mit allen Parametern
    public NotWalkableComponent(AStarGrid grid, int rangeX, int rangeY, int cellSize, int offsetX, int offsetY) {
        this.grid = grid;
        this.rangeX = Math.max(0, rangeX);
        this.rangeY = Math.max(0, rangeY);
        this.cellSize = cellSize;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void onAdded() {
        blockCells();
    }

    private void blockCells() {
        int cx = (int) Math.floor(entity.getX() / cellSize) + offsetX;
        int cy = (int) Math.floor(entity.getY() / cellSize) + offsetY;

        for (int y = cy; y <= cy + rangeY; y++) {
            for (int x = cx; x <= cx + rangeX; x++) {
                if (x >= 0 && x < grid.getWidth() && y >= 0 && y < grid.getHeight()) {
                    grid.get(x, y).setState(CellState.NOT_WALKABLE);
                } else {
                    System.err.println("Index out of bounds: (" + x + "," + y + ")");
                }
            }
        }
    }
}