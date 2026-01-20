package org.betriebssysteme.view;

import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import jdk.jfr.Event;

public class Camera {
    private Point2D lastDragPos;

    public void initCameraDrag() {
        var scene = FXGL.getPrimaryStage().getScene();
        Viewport vp = FXGL.getGameScene().getViewport();

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.MIDDLE) {   // oder PRIMARY / SECONDARY, wie du willst
                lastDragPos = new Point2D(e.getSceneX(), e.getSceneY());
            }
            if (e.getButton() == MouseButton.SECONDARY) {
                lastDragPos = new Point2D(e.getSceneX(), e.getSceneY());
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (e.isMiddleButtonDown() && lastDragPos != null) {

                double dxScreen = e.getSceneX() - lastDragPos.getX();
                double dyScreen = e.getSceneY() - lastDragPos.getY();

                double zoom = vp.getZoom();

                // Screen-Pixel in Weltverschiebung umrechnen
                double dxWorld = -dxScreen / zoom;
                double dyWorld = -dyScreen / zoom;

                vp.setX(vp.getX() + dxWorld);
                vp.setY(vp.getY() + dyWorld);

                lastDragPos = new Point2D(e.getSceneX(), e.getSceneY());
            }
            if (e.isSecondaryButtonDown() && lastDragPos != null) {

                double dxScreen = e.getSceneX() - lastDragPos.getX();
                double dyScreen = e.getSceneY() - lastDragPos.getY();

                double zoom = vp.getZoom();

                // Screen-Pixel in Weltverschiebung umrechnen
                double dxWorld = -dxScreen / zoom;
                double dyWorld = -dyScreen / zoom;

                vp.setX(vp.getX() + dxWorld);
                vp.setY(vp.getY() + dyWorld);

                lastDragPos = new Point2D(e.getSceneX(), e.getSceneY());
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getButton() == MouseButton.MIDDLE) {
                lastDragPos = null;
            }
            if (e.getButton() == MouseButton.SECONDARY) {
                lastDragPos = null;
            }
        });
    }
}
