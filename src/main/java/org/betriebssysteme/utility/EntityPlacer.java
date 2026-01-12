package org.betriebssysteme.utility;

import com.almasb.fxgl.entity.Entity;


public class EntityPlacer {
    public static void placeMachineAfterBelt(Entity machine, Entity belt, double offsetX, double offSetY) {
        machine.setPosition(belt.getX() + 27 + offsetX, belt.getY() -28 + offSetY);
    }

    public static void placeMachineAfterBelt(Entity machine, Entity belt) {
        EntityPlacer.placeMachineAfterBelt(machine, belt, 0.0, 0.0);
    }


}
