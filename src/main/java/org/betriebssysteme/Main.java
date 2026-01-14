package org.betriebssysteme;

import org.betriebssysteme.control.ProductionController;
import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.stations.Station;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Start the production line
        // All stations and personnel are created and initialized here
        ProductionController.createProductionLine();

        Map<Integer, Station> stations = ProductionHeadquarters.getInstance().getStations();
        // Print information about all stations
        for (Station station : stations.values()) {
            System.out.println("Station ID: " + station.getIdentificationNumber());
            String[][] infoArray = station.getInfoArray();
            for (String[] infoRow : infoArray) {
                System.out.println(infoRow[0] + ": " + infoRow[1]);
            }
            System.out.println("---------------------------");
        }
    }
}
