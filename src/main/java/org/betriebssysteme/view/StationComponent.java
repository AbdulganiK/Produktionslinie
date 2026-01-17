package org.betriebssysteme.view;

import com.almasb.fxgl.entity.component.Component;
import org.betriebssysteme.model.stations.Station;

import java.util.Arrays;

public class StationComponent extends Component {
    private final Station station;

    public StationComponent(Station station) {
        this.station = station;
    }

    public Station getStation() {
        return station;
    }

    @Override
    public void onUpdate(double tpf) {
        String[][] info = getStation().getInfoArray();
        String[] nameLabels  = new String[info.length];
        String[] valueLabels = new String[info.length];

        for (int i = 0; i < info.length; i++) {
            nameLabels[i]  = info[i][0];
            valueLabels[i] = info[i][1];
        }


        entity.getComponent(MenuComponent.class).setNameLabels(nameLabels);
        entity.getComponent(MenuComponent.class).setValueLabels(valueLabels);
    }
}
