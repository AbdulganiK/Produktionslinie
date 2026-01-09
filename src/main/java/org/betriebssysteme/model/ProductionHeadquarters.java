package org.betriebssysteme.model;

import org.betriebssysteme.model.personnel.Personnel;
import org.betriebssysteme.model.personnel.Supplier;
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


    public ProductionHeadquarters (){
        requestQueue = new PriorityQueue<Request>(Comparator.comparingInt(Request::priority).reversed());
        stations = new HashMap();
        personnel = new HashMap();
        logger = org.slf4j.LoggerFactory.getLogger("ProductionHeadquarters");
    }

    public ProductionHeadquarters(List<Station> stationsList, List<Personnel> personnelList){
        requestQueue = new PriorityQueue<>();
        stations = new HashMap();
        personnel = new HashMap();
        for (Station station : stationsList) {
            stations.put(station.getIdentificationNumber(), station);
        }
        for (Personnel person : personnelList) {
            personnel.put(person.getIdentificationNumber(), person);
        }
    }

    public void startAllPersonnel(){
        for (Object personObj : personnel.values()) {
            Personnel person = (Personnel) personObj;
            person.start();
        }
    }

    public void startAllStations(){
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

}
