package org.betriebssysteme.model;

import org.betriebssysteme.model.personnel.Personnel;
import org.betriebssysteme.model.stations.Station;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class ProductionHeadquarters{
    private PriorityQueue<Request> requestQueue;
    private Semaphore requestQueueSemaphore = new Semaphore(1);
    private Map stations;
    private Map personnel;

    public ProductionHeadquarters (){
        requestQueue = new PriorityQueue<>();
        stations = new HashMap();
        personnel = new HashMap();
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
