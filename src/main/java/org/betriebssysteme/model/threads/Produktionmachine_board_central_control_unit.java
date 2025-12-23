package org.betriebssysteme.model.threads;

import org.betriebssysteme.model.MaterialType;
import org.betriebssysteme.model.ProductionGood;
import org.betriebssysteme.model.Storage;

public class Produktionmachine_board_central_control_unit extends Machine {
    public Produktionmachine_board_central_control_unit(Storage storage,
                                                        int production_time_in_ms,
                                                        int material_storage_capacity,
                                                        int waiting_time_run_ms,
                                                        int material_priority_level) {

        MaterialType[] materialTypes = {MaterialType.CIRCUIT_BOARD_COMPONETS, MaterialType.DISPLAYS};
        int productionGoodStorageCapacity = 0; //Not needed for this machine
        int holding_area_capacity = 0; //Not needed for this machine

        super(storage,
                production_time_in_ms,
                material_storage_capacity,
                materialTypes, waiting_time_run_ms,
                material_priority_level,
                holding_area_capacity,
                productionGoodStorageCapacity);
    }

    @Override
    void dispatch_or_store_produced_goods(ProductionGood productionGood) {
        // This machine does not use productionGoodQueue, so we directly store the produced good
        // assembly line must handle the produced goods
    }

    @Override
    ProductionGood produce_good() {
        for (MaterialType materialType : material_storage.keySet()) {
            material_storage.get(materialType).poll();
        }
        return ProductionGood.CONTROL_PCB;
    }
}
