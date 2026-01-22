package org.betriebssysteme.view.components;

import com.almasb.fxgl.entity.component.Component;
import org.betriebssysteme.model.stations.Station;
import org.betriebssysteme.utility.Utility;

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
        Utility.setInfo(entity.getComponent(MenuComponent.class), getStation().getInfoArray());
    }
}
