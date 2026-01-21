package org.betriebssysteme.view;

import java.util.ArrayList;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import org.betriebssysteme.control.ProductionController;
import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.personnel.Personnel;
import org.betriebssysteme.model.personnel.Supplier;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.model.stations.Station;
import org.betriebssysteme.utility.EntityPlacer;

import org.betriebssysteme.view.animationtyps.MachineAnimationType;
import org.betriebssysteme.view.components.MachineComponent;
import org.betriebssysteme.view.factory.BeltDirection;
import org.betriebssysteme.view.factory.BeltFactory;
import org.betriebssysteme.view.factory.EntityNames;
import org.betriebssysteme.view.factory.EntityProductionLineFactory;

import java.util.*;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class Scenario {


    private EntityProductionLineFactory entityFactory;

    public Scenario(EntityProductionLineFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public void runFirst() {
        // Start the production line
        // All stations and personnel are created and initialized here
        ProductionController.createProductionLine();
        Map<Integer, Station> stations = ProductionHeadquarters.getInstance().getStations();
        Map<Integer, Personnel> personnels = ProductionHeadquarters.getInstance().getPersonnel();

        // Verschiebung Richtung Mitte
        final int OFFSET_X = 690;
        final int OFFSET_Y = 300;

        // Lager Spawnen
        SpawnData storageData = new SpawnData(100 + OFFSET_X, 100 + OFFSET_Y);
        storageData.put("station", stations.get(1));
        storageData.put("cellX", 23);
        storageData.put("cellY", 9);
        FXGL.spawn(EntityNames.STORAGE, storageData);

        // Lieferant Spawnen
        for (Personnel personnel : personnels.values()) {
            if (personnel instanceof Supplier) {
                SpawnData supplierData = new SpawnData(100 + OFFSET_X, 400 + OFFSET_Y);
                supplierData.put("supplier",  personnel);
                FXGL.spawn(EntityNames.SUPPLIER, supplierData);
            }
        }





        // Zentrale Spawnen
        SpawnData centralData = new SpawnData(1150 + OFFSET_X, 1050 + OFFSET_Y);
        centralData.put("station", ProductionHeadquarters.getInstance());
        centralData.put("cellX", 41);
        centralData.put("cellY", 26);
        FXGL.spawn(EntityNames.CENTRAL, centralData);

        // Lagerist Spawnen
        for (Personnel personnel : personnels.values()) {
            if (personnel instanceof WarehouseClerk) {
                SpawnData wareHouseSpawnData = new SpawnData(1150 + OFFSET_X, 1100 + OFFSET_Y);
                wareHouseSpawnData.put("personnel",  personnel);
                FXGL.spawn(EntityNames.WAREHOUSE_CLERK, wareHouseSpawnData);
            }
        }


        // Erste Reihe
        // -----------------------------------------
        // Erste Produktionsmaschine in der ersten Reihe
        SpawnData data = new SpawnData(500 + OFFSET_X, 700 + OFFSET_Y, 100);
        data.put("station", stations.get(21));
        data.put("cellX", 24);
        data.put("cellY", 21);
        Entity firstRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, data);

        firstRowProdmachine1.setZIndex(100);
        firstRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine und nach der Maschine
        BeltFactory.spawnBeltsBeforeMachine(firstRowProdmachine1, 3);
        var b = BeltFactory.spawnBeltsAfterMachine(firstRowProdmachine1, 7);
        firstRowProdmachine1.getComponent(MachineComponent.class).setBelt(b.get(1));
        entityFactory.spawnItemOnBelt(b.get(1));


        // zweite Produktionsmaschine in der ersten Reihe
        SpawnData machineData2 = new SpawnData(750 + OFFSET_X, 700 + OFFSET_Y, 100);
        machineData2.put("station", stations.get(22));
        machineData2.put("cellX", 29);
        machineData2.put("cellY", 21);
        Entity firstRowProdmachine2 = FXGL.spawn(EntityNames.MACHINE, machineData2);

        firstRowProdmachine2.setZIndex(100);
        firstRowProdmachine2.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine und nach der Maschine
        BeltFactory.spawnBeltsBeforeMachine(firstRowProdmachine2, 3);
        ArrayList<Entity> b1R1 = BeltFactory.spawnBeltsAfterMachine(firstRowProdmachine2, 3);
        firstRowProdmachine2.getComponent(MachineComponent.class).setBelt(b1R1.get(1));
        entityFactory.spawnItemOnBelt(b1R1.get(1));

        // belts zur zweiten Reihe
        ArrayList<Entity> bob1 = BeltFactory.spawnBeltOnBelt(b1R1.getLast(), 10, BeltDirection.HORIZONTAL);
        entityFactory.spawnItemOnBelt(bob1.get(3));


        // dritte Produktionsmaschine in der ersten Reihe
        SpawnData machineData3 = new SpawnData(1200 + OFFSET_X, 700 + OFFSET_Y, 100);
        machineData3.put("station", stations.get(24));
        machineData3.put("cellX", 38);
        machineData3.put("cellY", 21);
        Entity firstRowProdmachine3 = FXGL.spawn(EntityNames.MACHINE, machineData3);

        firstRowProdmachine3.setZIndex(100);
        firstRowProdmachine3.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine und nach der Maschine
        BeltFactory.spawnBeltsBeforeMachine(firstRowProdmachine3, 3);
        var b3 = BeltFactory.spawnBeltsAfterMachine(firstRowProdmachine3, 7);
        firstRowProdmachine3.getComponent(MachineComponent.class).setBelt(b3.get(1));

        // vierte Produktionsmaschine in der ersten Reihe
        SpawnData machineData4 = new SpawnData(1450 + OFFSET_X, 700 + OFFSET_Y, 100);
        machineData4.put("cellX", 43);
        machineData4.put("cellY", 21);
        machineData4.put("station", stations.get(25));
        Entity firstRowProdmachine4 = FXGL.spawn(EntityNames.MACHINE, machineData4);

        firstRowProdmachine4.setZIndex(100);
        firstRowProdmachine4.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine und nach der Maschine
        BeltFactory.spawnBeltsBeforeMachine(firstRowProdmachine4, 3);
        ArrayList<Entity> r1M4 = BeltFactory.spawnBeltsAfterMachine(firstRowProdmachine4, 3);
        firstRowProdmachine4.getComponent(MachineComponent.class).setBelt(r1M4.get(1));
        BeltFactory.spawnBeltOnBelt(r1M4.getLast(), 10, BeltDirection.HORIZONTAL);
        entityFactory.spawnItemOnBelt(r1M4.get(2));

        // Zweite Reihe
        // -----------------------------------------
        // Erste Produktionsmaschine in der zweiten Reihe
        SpawnData machineData5 = new SpawnData(700 + OFFSET_X, 400 + OFFSET_Y, 100);
        machineData5.put("cellX", 28);
        machineData5.put("cellY", 15);
        machineData5.put("station", stations.get(23));
        Entity seccondRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, machineData5);

        seccondRowProdmachine1.setZIndex(100);
        seccondRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine
        BeltFactory.spawnBeltsBeforeMachine(seccondRowProdmachine1, 2);

        // Belts nach der Maschine (analog erste Reihe)
        ArrayList<Entity> b1R2 = BeltFactory.spawnBeltsAfterMachine(seccondRowProdmachine1, 2);
        seccondRowProdmachine1.getComponent(MachineComponent.class).setBelt(b1R2.get(1));
        entityFactory.spawnItemOnBelt(b1R2.get(1));


        // Zweite Produktionsmaschine in der zweiten Reihe
        SpawnData machineData6 = new SpawnData(1400 + OFFSET_X, 400 + OFFSET_Y, 100);
        machineData6.put("cellX", 42);
        machineData6.put("cellY", 15);
        machineData6.put("station", stations.get(26));
        Entity seccondRowProdmachine2 = FXGL.spawn(EntityNames.MACHINE, machineData6);

        seccondRowProdmachine2.setZIndex(100);
        seccondRowProdmachine2.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine
        BeltFactory.spawnBeltsBeforeMachine(seccondRowProdmachine2, 2);

        // Belts nach der Maschine + Item wie bei den anderen
        ArrayList<Entity> b2R2 = BeltFactory.spawnBeltsAfterMachine(seccondRowProdmachine2, 2);
        seccondRowProdmachine2.getComponent(MachineComponent.class).setBelt(b2R2.get(1));
        entityFactory.spawnItemOnBelt(b2R2.get(1));

        // dritte Reihe
        // -----------------------------------------

        // Erste KontrollMascine dritte Reihe
        SpawnData machineData7 = new SpawnData(700 + OFFSET_X, 400 + OFFSET_Y, 100);
        machineData7.put("cellX", 31);
        machineData7.put("cellY", 14);
        machineData7.put("station", stations.get(31));
        Entity thirdRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, machineData7);

        thirdRowProdmachine1.setZIndex(100);
        thirdRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // Maschine direkt an Band PLatzieren
        EntityPlacer.placeMachineAfterBelt(thirdRowProdmachine1, b1R2.getLast());

        // Band nach Maschine
        var productBelt = BeltFactory.spawnBeltsAfterMachine(thirdRowProdmachine1, 17);
        thirdRowProdmachine1.getComponent(MachineComponent.class).setBelt(productBelt.get(1));
        entityFactory.spawnItemOnBelt(productBelt.get(4));
        entityFactory.spawnItemOnBelt(productBelt.get(8));
        entityFactory.spawnItemOnBelt(productBelt.get(6));
        entityFactory.spawnItemOnBelt(productBelt.get(10));


        // Zweite KotnrollMaschine dritte Reihe
        SpawnData machineData8 = new SpawnData(700 + OFFSET_X, 400 + OFFSET_Y, 100);
        machineData8.put("cellX", 45);
        machineData8.put("cellY", 14);
        machineData8.put("station", stations.get(32));
        Entity thirdRowProdmachine2 = FXGL.spawn(EntityNames.MACHINE, machineData8);

        thirdRowProdmachine2.setZIndex(100);
        thirdRowProdmachine2.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // Maschine direkt an Band Platzieren
        EntityPlacer.placeMachineAfterBelt(thirdRowProdmachine2, b2R2.getLast());
        var sideProductbelt = BeltFactory.spawnBeltsAfterMachine(thirdRowProdmachine2, 2);
        thirdRowProdmachine2.getComponent(MachineComponent.class).setBelt(sideProductbelt.get(1));
        entityFactory.spawnItemOnBelt(sideProductbelt.get(1));
        BeltFactory.spawnBeltOnBelt(sideProductbelt.getLast(), 10, BeltDirection.HORIZONTAL);

        // vierte Reihe
        // -----------------------------------------
        // Erste KontrollMascine dritte Reihe
        SpawnData machineData9 = new SpawnData(700 + OFFSET_X, 400 + OFFSET_Y, 100);
        machineData9.put("cellX", 42);
        machineData9.put("cellY", 8);
        machineData9.put("station", stations.get(41));
        Entity fourthRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, machineData9);

        fourthRowProdmachine1.setZIndex(100);
        fourthRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        EntityPlacer.placeMachineAfterBelt(fourthRowProdmachine1, productBelt.getLast());


    }




}
