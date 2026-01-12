package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import org.betriebssysteme.utility.EntityPlacer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Scenario {

    private EntityProductionLineFactory entityFactory;

    public Scenario(EntityProductionLineFactory entityFactory) {
        this.entityFactory = entityFactory;
    }




    public void runFirst() {
        // Erste Reihe
        // -----------------------------------------
        // Erste Produktionsmaschine in der ersten Reihe
        Entity firstRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, 500, 700);
        firstRowProdmachine1.setZIndex(100);
        firstRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine und nach der Maschine
        BeltFactory.spawnBeltsBeforeMachine(firstRowProdmachine1, 3);
        var b = BeltFactory.spawnBeltsAfterMachine(firstRowProdmachine1, 7);
        entityFactory.spawnItemOnBelt(b.get(1));


        // zweite Produktionsmaschine in der ersten Reihe
        Entity firstRowProdmachine2 = FXGL.spawn(EntityNames.MACHINE, 750, 700);
        firstRowProdmachine2.setZIndex(100);
        firstRowProdmachine2.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine und nach der Maschine
        BeltFactory.spawnBeltsBeforeMachine(firstRowProdmachine2, 3);
        ArrayList<Entity> b1R1 = BeltFactory.spawnBeltsAfterMachine(firstRowProdmachine2, 3);
        entityFactory.spawnItemOnBelt(b1R1.get(1));

        // belts zur zweiten Reihe
        ArrayList<Entity> bob1 = BeltFactory.spawnBeltOnBelt(b1R1.getLast(), 10, BeltDirection.HORIZONTAL);
        entityFactory.spawnItemOnBelt(bob1.get(3));


        // dritte Produktionsmaschine in der ersten Reihe
        Entity firstRowProdmachine3 = FXGL.spawn(EntityNames.MACHINE, 1200, 700);
        firstRowProdmachine3.setZIndex(100);
        firstRowProdmachine3.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine und nach der Maschine
        BeltFactory.spawnBeltsBeforeMachine(firstRowProdmachine3, 3);
        BeltFactory.spawnBeltsAfterMachine(firstRowProdmachine3, 7);

        // vierte Produktionsmaschine in der ersten Reihe
        Entity firstRowProdmachine4 = FXGL.spawn(EntityNames.MACHINE, 1450, 700);
        firstRowProdmachine4.setZIndex(100);
        firstRowProdmachine4.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine und nach der Maschine
        BeltFactory.spawnBeltsBeforeMachine(firstRowProdmachine4, 3);
        ArrayList<Entity> r1M4 = BeltFactory.spawnBeltsAfterMachine(firstRowProdmachine4, 3);
        BeltFactory.spawnBeltOnBelt(r1M4.getLast(), 10, BeltDirection.HORIZONTAL);
        entityFactory.spawnItemOnBelt(r1M4.get(2));

        // Zweite Reihe
        // -----------------------------------------
        // Erste Produktionsmaschine in der zweiten Reihe
        Entity seccondRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, 700, 400);
        seccondRowProdmachine1.setZIndex(100);
        seccondRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine
        BeltFactory.spawnBeltsBeforeMachine(seccondRowProdmachine1, 2);

        // Belts nach der Maschine (analog erste Reihe)
        ArrayList<Entity> b1R2 = BeltFactory.spawnBeltsAfterMachine(seccondRowProdmachine1, 1);
        entityFactory.spawnItemOnBelt(b1R2.get(1));


        // Zweite Produktionsmaschine in der zweiten Reihe
        Entity seccondRowProdmachine2 = FXGL.spawn(EntityNames.MACHINE, 1400, 400);
        seccondRowProdmachine2.setZIndex(100);
        seccondRowProdmachine2.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine
        BeltFactory.spawnBeltsBeforeMachine(seccondRowProdmachine2, 2);

        // Belts nach der Maschine + Item wie bei den anderen
        ArrayList<Entity> b2R2 = BeltFactory.spawnBeltsAfterMachine(seccondRowProdmachine2, 1);
        entityFactory.spawnItemOnBelt(b2R2.get(1));

        // dritte Reihe
        // -----------------------------------------

        // Erste KontrollMascine dritte Reihe
        Entity thirdRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, 700, 400);
        thirdRowProdmachine1.setZIndex(100);
        thirdRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // Maschine direkt an Band PLatzieren
        EntityPlacer.placeMachineAfterBelt(thirdRowProdmachine1, b1R2.getLast());

        // Band nach Maschine
        var productBelt = BeltFactory.spawnBeltsAfterMachine(thirdRowProdmachine1, 16);
        entityFactory.spawnItemOnBelt(productBelt.get(4));
        entityFactory.spawnItemOnBelt(productBelt.get(8));
        entityFactory.spawnItemOnBelt(productBelt.get(6));
        entityFactory.spawnItemOnBelt(productBelt.get(10));


        // Zweite KotnrollMaschine dritte Reihe
        Entity thirdRowProdmachine2 = FXGL.spawn(EntityNames.MACHINE, 700, 400);
        thirdRowProdmachine2.setZIndex(100);
        thirdRowProdmachine2.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // Maschine direkt an Band Platzieren
        EntityPlacer.placeMachineAfterBelt(thirdRowProdmachine2, b2R2.getLast());
        var sideProductbelt = BeltFactory.spawnBeltsAfterMachine(thirdRowProdmachine2, 2);
        entityFactory.spawnItemOnBelt(sideProductbelt.get(1));
        BeltFactory.spawnBeltOnBelt(sideProductbelt.getLast(), 10, BeltDirection.HORIZONTAL);

        // vierte Reihe
        // -----------------------------------------
        // Erste KontrollMascine dritte Reihe
        Entity fourthRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, 700, 400);
        fourthRowProdmachine1.setZIndex(100);
        fourthRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        EntityPlacer.placeMachineAfterBelt(fourthRowProdmachine1, productBelt.getLast());

        BeltFactory.spawnBeltsAfterMachine(fourthRowProdmachine1, 1);




    }

}
