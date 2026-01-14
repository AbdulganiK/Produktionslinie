package org.betriebssysteme.model;

import org.betriebssysteme.model.personnel.Personnel;
import org.betriebssysteme.model.personnel.Supplier;
import org.betriebssysteme.model.stations.Maschine;
import org.betriebssysteme.model.stations.Station;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.Semaphore;

public class ProductionHeadquarters{
    private PriorityQueue<Request> requestQueue;
    private Semaphore requestQueueSemaphore = new Semaphore(1);
    private Map stations;
    private Map personnel;
    private Logger logger;
    private static ProductionHeadquarters singletonInstance;


    private ProductionHeadquarters (){
        requestQueue = new PriorityQueue<Request>(Comparator.comparingInt(Request::priority).reversed());
        this.stations = new HashMap();
        this.personnel = new HashMap();
        logger = org.slf4j.LoggerFactory.getLogger("ProductionHeadquarters");
    }

    public static ProductionHeadquarters getInstance(){
        if (singletonInstance == null){
            singletonInstance = new ProductionHeadquarters();
        }
        return singletonInstance;
    }

    public void startAllPersonnel(){
        for (Object personObj : personnel.values()) {
            Personnel person = (Personnel) personObj;
            person.start();
            logger.info("Started personnel with ID: " + person.getIdentificationNumber());
        }
    }

    public void startAllStations(){
        for (Object stationObj : stations.values()) {
            Station station = (Station) stationObj;
            stations.put(station.getIdentificationNumber(), station);
        }
        for (Object stationObj : stations.values()) {
            Station station = (Station) stationObj;
            station.start();
            logger.info("Started station with ID: " + station.getIdentificationNumber());
        }
    }

    public void addRequest(Request request){
        requestQueueSemaphore.acquireUninterruptibly();
        requestQueue.add(request);
        requestQueueSemaphore.release();
    }

    public Request pollRequest(){
        Request request;
        requestQueueSemaphore.acquireUninterruptibly();
        request = requestQueue.poll();
        requestQueueSemaphore.release();
        return request;
    }

    public Map getStations(){
        Map<Integer, Station> stationsMap = new HashMap<>();
        for (Object stationObj : stations.values()) {
            Station station = (Station) stationObj;
            stationsMap.put(station.getIdentificationNumber(), station);
        }
        System.out.println("Stations map retrieved with " + stationsMap.size() + " stations.");
        return stationsMap;
    }

    public Map getPersonnel(){
        return personnel;
    }

    public PriorityQueue<Request> getRequestQueue(){
        return requestQueue;
    }

    public void addStation(Station station) {
        stations.put(station.getIdentificationNumber(), station);
    }

    public void addPersonnel(Personnel person) {
        personnel.put(person.getIdentificationNumber(), person);
    }

}
