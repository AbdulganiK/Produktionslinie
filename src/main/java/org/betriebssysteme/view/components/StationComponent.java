package org.betriebssysteme.view.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import org.betriebssysteme.model.stations.MainDepot;
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
        if (station instanceof MainDepot) {
            entity.getComponent(StatusComponent.class).setColorFirstLamp(Color.rgb(25, 25, 30, 0.95));
            entity.getComponent(StatusComponent.class).setColorSecondLamp(station.getStatus().getStatusTyp());
        }
        Utility.setInfo(entity.getComponent(MenuComponent.class), getStation().getInfoArray());
    }
}
