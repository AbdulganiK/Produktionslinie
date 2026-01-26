package org.betriebssysteme.view.factory;

import java.util.concurrent.ThreadLocalRandom;

public enum SupplierSkin {
    GREEN("Supplier_Front.png","Supplier_Back.png","Supplier_Right.png","Supplier_Left.png");

    public final String front, back, right, left;

    SupplierSkin(String front, String back, String right, String left) {
        this.front = front;
        this.back = back;
        this.right = right;
        this.left = left;
    }
}
