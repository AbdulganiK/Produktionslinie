package org.betriebssysteme.view;

import java.util.concurrent.ThreadLocalRandom;

public enum WarehouseClerkSkin {
    BLOND("Blondworker_Front.png"),
    BLUE("Blueworker_Front.png"),
    RED("Redworker_Front.png");

    private final String spriteSheetFile;

    WarehouseClerkSkin(String spriteSheetFile) {
        this.spriteSheetFile = spriteSheetFile;
    }

    public String file(){
        return spriteSheetFile;
    }

    public static WarehouseClerkSkin random(){
        WarehouseClerkSkin[] all = values();
        return all[ThreadLocalRandom.current().nextInt(all.length)];
    }
}
