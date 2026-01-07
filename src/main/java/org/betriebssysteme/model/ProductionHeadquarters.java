package org.betriebssysteme.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class ProductionHeadquarters{
    PriorityQueue<Request> requestQueue;
    Semaphore requestQueueSemaphore = new Semaphore(1);
    Map stations;
    Map personnel;

    ProductionHeadquarters (){
        requestQueue = new PriorityQueue<>();
        stations = new HashMap();
        personnel = new HashMap();
    }

    ProductionHeadquarters(List<Station> stationsList, List<Personnel> personnelList){
        requestQueue = new PriorityQueue<>();
        stations = new HashMap();
        personnel = new HashMap();
        for (Station station : stationsList) {
            stations.put(station.getId(), station);
        }
        for (Personnel person : personnelList) {
            personnel.put(person.getId(), person);
        }
    }

    void addRequest(Request request){
        requestQueueSemaphore.acquireUninterruptibly();
        requestQueue.add(request);
        requestQueueSemaphore.release();
    }

    Request pollRequest(){
        Request request;
        requestQueueSemaphore.acquireUninterruptibly();
        request = requestQueue.poll();
        requestQueueSemaphore.release();
        return request;
    }

    Map getStations(){
        return stations;
    }

    Map getPersonnel(){
        return personnel;
    }

    PriorityQueue<Request> getRequestQueue(){
        return requestQueue;
    }

    void addStation(Station station) {
        stations.put(station.getId(), station);
    }

    void addPersonnel(Personnel person) {
        personnel.put(person.getId(), person);
    }

}
