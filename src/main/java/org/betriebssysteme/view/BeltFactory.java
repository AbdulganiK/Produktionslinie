package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import java.util.ArrayList;

public class BeltFactory {

    // Ein Schritt von einem Belt zum nächsten
    private static final double STEP_X = 27;
    private static final double STEP_Y = -13;

    // Abstand vom letzten Belt (END) zur Maschine
    // aus deinem Beispiel mit 10 Belts hergeleitet:
    // Maschine (1000,1000), letzter Belt (973,1027) -> +27, -27
    private static final double INPUT_END_OFFSET_X = 27;
    private static final double INPUT_END_OFFSET_Y = -27;

    // Abstand vom ersten Belt HINTER der Maschine (Start der Out-Linie)
    private static final double OUTPUT_START_OFFSET_X = 27;   // musst du ggf. feinjustieren
    private static final double OUTPUT_START_OFFSET_Y = -5;

    private static ArrayList<Entity> spawnBeltLine(int length, double startX, double startY, int startZ) {
        ArrayList<Entity> entities = new ArrayList<>();
        double x = startX;
        double y = startY;
        int z = startZ;

        // START
        Entity start = FXGL.spawn(EntityNames.BELT, x, y);
        start.setZIndex(z--);
        start.getComponent(BeltComponent.class).setAnimation(BeltAnimationType.START);
        entities.add(start);

        // MIDDLE
        for (int i = 0; i < length; i++) {
            x += STEP_X;
            y += STEP_Y;

            Entity middle = FXGL.spawn(EntityNames.BELT, x, y);
            middle.setZIndex(z--);
            middle.getComponent(BeltComponent.class).setAnimation(BeltAnimationType.MID);
            entities.add(middle);
        }

        // END
        x += STEP_X;
        y += STEP_Y;

        Entity end = FXGL.spawn(EntityNames.BELT, x, y);
        end.setZIndex(z);
        end.getComponent(BeltComponent.class).setAnimation(BeltAnimationType.END);
        entities.add(end);
        return entities;
    }

    public static ArrayList<Entity> spawnBeltsAfterMachine(Entity machine, int length) {

        double startX = machine.getX() + OUTPUT_START_OFFSET_X;
        double startY = machine.getY() + OUTPUT_START_OFFSET_Y;

        return spawnBeltLine(length, startX, startY, machine.getZIndex() - length - 1);
    }

    public static ArrayList<Entity> spawnBeltsBeforeMachine(Entity machine, int length) {

        double machineX = machine.getX();
        double machineY = machine.getY();

        double startX = machineX - INPUT_END_OFFSET_X - (length + 1) * STEP_X;
        double startY = machineY - INPUT_END_OFFSET_Y - (length + 1) * STEP_Y;

        return spawnBeltLine(length, startX, startY, machine.getZIndex() + length + 1);
    }


}
