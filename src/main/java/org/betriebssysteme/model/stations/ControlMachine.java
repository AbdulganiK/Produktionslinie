package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.cargo.Cargo;

import java.util.Map;

public class ControlMachine extends Maschine{
    public ControlMachine(int identificationNumber,
                          int timeToSleep,
                          int maxStorageCapacity,
                          Map<Cargo, Integer> initialStorage,
                          Cargo productCargo,
                          Maschine nextMaschine,
                          int productionTime,
                          ProductionHeadquarters productionHeadquarters) {
        super(identificationNumber,
                productionTime,
                timeToSleep,
                maxStorageCapacity,
                productionHeadquarters,
                nextMaschine,
                initialStorage,
                productCargo);
    }

    @Override
    protected void checkStorageStatus() {

    }

    @Override
    protected void checkIfCargoPrductionIsPossible() {

    }

    @Override
    protected Cargo produceProduct() {
        return null;
    }

    @Override
    protected void storePrductOrDeliverToNextMachine(Cargo cargo) {
        logger.info("ControlMachine " + identificationNumber + " storing product: " + cargo);
    }
}
