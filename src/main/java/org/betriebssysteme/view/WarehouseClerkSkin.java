package org.betriebssysteme.view;

import java.util.concurrent.ThreadLocalRandom;

public enum WarehouseClerkSkin {
    BLOND("Blondworker_Front.png","Blondworker_Back.png","Blondworker_Right.png","Blondworker_Left.png"),
    BLUE("Blueworker_Front.png","Blueworker_Back.png","Blueworker_Right.png","Blueworker_Left.png"),
    RED("Redworker_Front.png","Redworker_Back.png","Redworker_Right.png","Redworker_Left.png");

    public final String front, back, right, left;

    WarehouseClerkSkin(String front, String back, String right, String left) {
        this.front = front;
        this.back = back;
        this.right = right;
        this.left = left;
    }

    public static WarehouseClerkSkin random(){
       var all = values();
        return all[java.util.concurrent.ThreadLocalRandom.current().nextInt(all.length)];
    }
}
