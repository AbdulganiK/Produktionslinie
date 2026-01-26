package org.betriebssysteme.model;

import org.betriebssysteme.model.personnel.Personnel;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.model.stations.Station;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.Semaphore;

public class ProductionHeadquarters{
    private PriorityQueue<Request> requestQueue;
    private Semaphore requestQueueSemaphore = new Semaphore(1);
    private Map stations;
    private Map personnel;
    private static Logger logger;
    private static ProductionHeadquarters singletonInstance;
    private int identificationNumber;


    /**
     * Private constructor for singleton pattern
     */
    private ProductionHeadquarters (){
        requestQueue = new PriorityQueue<Request>(Comparator.comparingInt(Request::priority).reversed());
        this.stations = new HashMap();
        this.personnel = new HashMap();
        this.identificationNumber = 0;
        logger = org.slf4j.LoggerFactory.getLogger("ProductionHeadquarters");
    }

    /**
     * Get the singleton instance of ProductionHeadquarters
     * @return ProductionHeadquarters instance
     */
    public static ProductionHeadquarters getInstance(){
        if (singletonInstance == null){
            singletonInstance = new ProductionHeadquarters();
            logger.info("Production Headquarters instance created");
        }
        return singletonInstance;
    }

    /**
     * Start all personnel threads
     */
    public void startAllPersonnel(){
        for (Object personObj : personnel.values()) {
            Personnel person = (Personnel) personObj;
            person.start();
            logger.info("Started personnel with ID: " + person.getIdentificationNumber());
        }
    }

    /**
     * Start all station threads
     */
    public void startAllStations(){
        for (Object stationObj : stations.values()) {
            Station station = (Station) stationObj;
            station.start();
            logger.info("Started station with ID: " + station.getIdentificationNumber());
        }
    }

    /**
     * Add a request to the request queue
     * This method is thread-safe, as it uses a semaphore to control access to the request queue.
     * @param request Request to be added
     */
    public void addRequest(Request request){
        requestQueueSemaphore.acquireUninterruptibly();
        requestQueue.add(request);
        requestQueueSemaphore.release();
    }

    /**
     * Poll a request from the request queue
     * This method is thread-safe, as it uses a semaphore to control access to the request queue.
     * @return Polled Request
     */
    public Request pollRequest(){
        Request request;
        requestQueueSemaphore.acquireUninterruptibly();
        request = requestQueue.poll();
        requestQueueSemaphore.release();
        return request;
    }


    public Map getStations(){
        return stations;
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

    public int getIdentificationNumber() {
        return identificationNumber;
    }

    public void deliteAllData() {
        stations.clear();
        personnel.clear();
        try {
            requestQueueSemaphore.acquire();
            requestQueue.clear();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            requestQueueSemaphore.release();
        }
        logger.info("All stations and personnel have been deleted from Production Headquarters");
    }

}
