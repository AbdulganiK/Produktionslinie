package org.betriebssysteme.view;

import java.util.List;
import java.util.ArrayList;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import org.betriebssysteme.control.ProductionController;
import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.model.stations.Station;
import org.betriebssysteme.utility.EntityPlacer;

import javafx.geometry.Point2D;
import java.util.*;
import java.util.stream.Collectors;

public class Scenario {
    public static final Map<Integer, Point2D> STATION_POS = new HashMap<>();
    public static final Map<Integer, Point2D> STATION_ARRIVAL_POS = new HashMap<>();

    private void rememberStationPos(int stationId, double x, double y, double offsetX, double offsetY) {
        Point2D p = new Point2D(x, y);
        STATION_POS.put(stationId, p);
        STATION_ARRIVAL_POS.put(stationId, p.add(offsetX, offsetY));
    }

    public static final double LEFT_X  = 250;
    public static final double MID_X   = 1000;
    public static final double RIGHT_X = 1500;
    public static final double BOTTOM_Y = 820;
    public static final double TOP_Y = 520;

    public static final Map<Integer, List<Point2D>> PATH_TO_STATION = new HashMap<>();

    private void rememberPathToStation(int stationId, List<Point2D> waypoints) {
        PATH_TO_STATION.put(stationId, waypoints);
    }

    private void buildRoutes() {

        Point2D LEFT_BOTTOM  = new Point2D(LEFT_X,  BOTTOM_Y);
        Point2D MID_BOTTOM   = new Point2D(MID_X,   BOTTOM_Y);
        Point2D RIGHT_BOTTOM = new Point2D(RIGHT_X, BOTTOM_Y);

        Point2D LEFT_TOP  = new Point2D(LEFT_X,  TOP_Y);
        Point2D MID_TOP   = new Point2D(MID_X,   TOP_Y);
        Point2D RIGHT_TOP = new Point2D(RIGHT_X, TOP_Y);

        java.util.function.BiFunction<Integer, Double, Point2D> underStation =
                (id, corridorY) -> new Point2D(STATION_ARRIVAL_POS.get(id).getX(), corridorY);

        rememberPathToStation(21, List.of(underStation.apply(21, BOTTOM_Y)));
        rememberPathToStation(22, List.of(underStation.apply(22, BOTTOM_Y)));
        rememberPathToStation(24, List.of(underStation.apply(24, BOTTOM_Y)));
        rememberPathToStation(25, List.of(underStation.apply(25, BOTTOM_Y)));

        rememberPathToStation(31, List.of(
                LEFT_BOTTOM,
                MID_BOTTOM,
                underStation.apply(31, BOTTOM_Y)
        ));

        rememberPathToStation(26, List.of(
                LEFT_BOTTOM,
                RIGHT_BOTTOM,
                underStation.apply(26, BOTTOM_Y)
        ));

        rememberPathToStation(32, List.of(
                LEFT_BOTTOM,
                RIGHT_BOTTOM,
                underStation.apply(32, BOTTOM_Y)
        ));

        rememberPathToStation(41, List.of(
                LEFT_BOTTOM,
                RIGHT_BOTTOM,
                underStation.apply(41, BOTTOM_Y)
        ));

        rememberPathToStation(23, List.of(
                LEFT_TOP,
                MID_TOP,
                underStation.apply(23, TOP_Y)
        ));
    }

    private EntityProductionLineFactory entityFactory;

    public Scenario(EntityProductionLineFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public void runFirst() {
        // Start the production line
        // All stations and personnel are created and initialized here
        ProductionController.createProductionLine();
        Map<Integer, Station> stations = ProductionHeadquarters.getInstance().getStations();

        // WarehouseClerks spawnen
        List<WarehouseClerk> clerks = (List<WarehouseClerk>) ProductionHeadquarters.getInstance()
                .getPersonnel()
                .values()
                .stream()
                .filter(p -> p instanceof WarehouseClerk)
                .map(p -> (WarehouseClerk) p)
                .collect(Collectors.toList());

        double startX = 200;
        double startY = 200;
        double spacing = 70;

        for (int i = 0; i < clerks.size(); i++) {
            SpawnData clerkData = new SpawnData(startX + i * spacing, startY);
            clerkData.put("clerk", clerks.get(i));
            Entity clerk = FXGL.spawn(EntityNames.WAREHOUSE_CLERK, clerkData);
            clerk.setZIndex(1000);
        }

        // Lager Spawnen
        SpawnData storageData = new SpawnData(100, 100);
        storageData.put("station", stations.get(1));
        FXGL.spawn(EntityNames.STORAGE, storageData);

        rememberStationPos(1, 100, 100, 0, 10);

        // === Supplier spawnen ===
        var suppliers = (List<org.betriebssysteme.model.personnel.Supplier>) ProductionHeadquarters.getInstance()
                .getPersonnel()
                .values()
                .stream()
                .filter(p -> p instanceof org.betriebssysteme.model.personnel.Supplier)
                .map(p -> (org.betriebssysteme.model.personnel.Supplier) p)
                .collect(Collectors.toList());

        Point2D storagePos = Scenario.STATION_POS.get(1);
        double sp = 80;

        double supplierY = storagePos.getY() + 120;

        double supplierStartX = storagePos.getX() - ((suppliers.size() - 1) * sp) / 2.0;

        for (int i = 0; i < suppliers.size(); i++) {
            SpawnData sd = new SpawnData(supplierStartX + i * sp, supplierY);
            sd.put("supplier", suppliers.get(i));
            Entity e = FXGL.spawn(EntityNames.SUPPLIER, sd);
            e.setZIndex(1000);
        }

        // Zentrale Spawnen
        FXGL.spawn(EntityNames.CENTRAL, 1150, 1050);


        // Erste Reihe
        // -----------------------------------------
        // Erste Produktionsmaschine in der ersten Reihe
        SpawnData data = new SpawnData(500, 700, 100);
        data.put("station", stations.get(21));
        Entity firstRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, data);

        rememberStationPos(21, 500, 700, 0, 30);

        firstRowProdmachine1.setZIndex(100);
        firstRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine und nach der Maschine
        BeltFactory.spawnBeltsBeforeMachine(firstRowProdmachine1, 3);
        var b = BeltFactory.spawnBeltsAfterMachine(firstRowProdmachine1, 7);
        entityFactory.spawnItemOnBelt(b.get(1));


        // zweite Produktionsmaschine in der ersten Reihe
        SpawnData machineData2 = new SpawnData(750, 700, 100);
        machineData2.put("station", stations.get(22));
        Entity firstRowProdmachine2 = FXGL.spawn(EntityNames.MACHINE, machineData2);

        rememberStationPos(22, 750, 700, 0, 30);

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
        SpawnData machineData3 = new SpawnData(1200, 700, 100);
        machineData3.put("station", stations.get(24));
        Entity firstRowProdmachine3 = FXGL.spawn(EntityNames.MACHINE, machineData3);

        rememberStationPos(24, 1200, 700, 0, 30);

        firstRowProdmachine3.setZIndex(100);
        firstRowProdmachine3.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine und nach der Maschine
        BeltFactory.spawnBeltsBeforeMachine(firstRowProdmachine3, 3);
        BeltFactory.spawnBeltsAfterMachine(firstRowProdmachine3, 7);

        // vierte Produktionsmaschine in der ersten Reihe
        SpawnData machineData4 = new SpawnData(1450, 700, 100);
        machineData4.put("station", stations.get(25));
        Entity firstRowProdmachine4 = FXGL.spawn(EntityNames.MACHINE, machineData4);

        rememberStationPos(25, 1450, 700, 0, 30);

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
        SpawnData machineData5 = new SpawnData(700, 400, 100);
        machineData5.put("station", stations.get(23));
        Entity seccondRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, machineData5);

        rememberStationPos(23, 700, 400, 0, 30);

        seccondRowProdmachine1.setZIndex(100);
        seccondRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        // 3 Belts vor der Maschine
        BeltFactory.spawnBeltsBeforeMachine(seccondRowProdmachine1, 2);

        // Belts nach der Maschine (analog erste Reihe)
        ArrayList<Entity> b1R2 = BeltFactory.spawnBeltsAfterMachine(seccondRowProdmachine1, 1);
        entityFactory.spawnItemOnBelt(b1R2.get(1));


        // Zweite Produktionsmaschine in der zweiten Reihe
        SpawnData machineData6 = new SpawnData(1400, 400, 100);
        machineData6.put("station", stations.get(26));
        Entity seccondRowProdmachine2 = FXGL.spawn(EntityNames.MACHINE, machineData6);

        rememberStationPos(26, 1400, 400, 0, 30);

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
        SpawnData machineData7 = new SpawnData(700, 400, 100);
        machineData7.put("station", stations.get(31));
        Entity thirdRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, machineData7);

        rememberStationPos(31, 700, 400, 0, 30);

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
        SpawnData machineData8 = new SpawnData(700, 400, 100);
        machineData8.put("station", stations.get(32));
        Entity thirdRowProdmachine2 = FXGL.spawn(EntityNames.MACHINE, machineData8);

        rememberStationPos(32, 700, 400, 0, 30);

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
        SpawnData machineData9 = new SpawnData(700, 400, 100);
        machineData9.put("station", stations.get(41));
        Entity fourthRowProdmachine1 = FXGL.spawn(EntityNames.MACHINE, machineData9);

        rememberStationPos(41, 700, 400, 0, 30);

        fourthRowProdmachine1.setZIndex(100);
        fourthRowProdmachine1.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);

        EntityPlacer.placeMachineAfterBelt(fourthRowProdmachine1, productBelt.getLast());

        BeltFactory.spawnBeltsAfterMachine(fourthRowProdmachine1, 1);


        // Path bestimmen
        buildRoutes();
    }

}
