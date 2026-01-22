package org.betriebssysteme.utility;

import org.betriebssysteme.view.components.MenuComponent;

public class Utility {
    public static void setInfo(MenuComponent menu, String[][] info) {
        String[] nameLabels  = new String[info.length];
        String[] valueLabels = new String[info.length];

        for (int i = 0; i < info.length; i++) {
            nameLabels[i]  = info[i][0];
            valueLabels[i] = info[i][1];
        }

        menu.setNameLabels(nameLabels);
        menu.setValueLabels(nameLabels);


    }
}
