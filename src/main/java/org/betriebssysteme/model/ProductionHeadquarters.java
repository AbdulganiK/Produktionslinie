package org.betriebssysteme.model;

import org.betriebssysteme.model.personnel.Personnel;
import org.betriebssysteme.model.stations.Station;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.Semaphore;

public class ProductionHeadquarters{
    /**
     * The PriorityQueue requestQueue is used to manage the requests in the productionline.
     * Each request has a priority level, and the queue ensures that higher priority requests are processed before lower priority ones.
     * The Comparator is used to sort the requests based on their priority in descending order
     * (higher priority first).
     * It is synchronized
     * using a Semaphore to ensure thread safety when multiple personnel and stations access the queue concurrently.
     * The Semaphore allows only one thread to access the requestQueue at a time,
     * preventing multiple threads from modifying the queue simultaneously
     * and causing data corruption or inconsistencies.
     */
    private final PriorityQueue<Request> requestQueue;
    private final Semaphore requestQueueSemaphore = new Semaphore(1);
    // The stations HashMap stores the stations in the production line, with the station's identification number as the key and the Station object as the value.
    private final HashMap stations;
    // The personnel HashMap stores the personnel in the production line, with the personnel's identification number as the key and the Personnel object as the value.
    private final HashMap personnel;
    private static Logger logger;
    // Singleton instance of ProductionHeadquarters
    private static ProductionHeadquarters singletonInstance;
    private final int identificationNumber;
    private boolean consoleOutputEnabled = false;


    /**
     * Private constructor for a singleton pattern
     */
    private ProductionHeadquarters (){
        requestQueue = new PriorityQueue<>(Comparator.comparingInt(Request::priority).reversed());
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
            logger.info("Started personnel with ID: {}", person.getIdentificationNumber());
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
     * This method is thread-safe, as it uses semaphore to control access to the request queue.
     * @param request Request to be added
     */
    public void addRequest(Request request){
        requestQueueSemaphore.acquireUninterruptibly();
        requestQueue.add(request);
        requestQueueSemaphore.release();
    }

    /**
     * Poll a request from the request queue
     * This method is thread-safe, as it uses semaphore to control access to the request queue.
     * @return Polled Request
     */
    public Request pollRequest(){
        Request request;
        requestQueueSemaphore.acquireUninterruptibly();
        request = requestQueue.poll();
        requestQueueSemaphore.release();
        return request;
    }

    //============================================================================
    // Getters and Setters
    public Map getStations(){
        return stations;
    }

    public Map getPersonnel(){
        return personnel;
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

    public boolean isConsoleOutputEnabled() {
        return consoleOutputEnabled;
    }

    public void setConsoleOutputEnabled(boolean consoleOutputEnabled) {
        this.consoleOutputEnabled = consoleOutputEnabled;
    }

    //============================================================================
    /**
     * The deliteAllData method is used to clear all stations, personnel, and requests from the Production Headquarters.
     * It clears the stations and personnel HashMaps and also clears the request queue while ensuring thread safety using the semaphore.
     * This method can be useful for resetting the state of the Production Headquarters or for loading new configurations without restarting the application.
     * @throws RuntimeException if the thread is interrupted while acquiring the semaphore to clear the request queue.
     */
    public void deleteAllData() {
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
