package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import org.betriebssysteme.model.threads.Machine;

import java.awt.*;

public class EntityProductionLineFactory implements EntityFactory {

    @Spawns(EntityNames.MACHINE)
    public Entity newMachine(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.MACHINE)
                .viewWithBBox(new Rectangle(40, 40,  Color.BLUE))
                .build();
    }

}
