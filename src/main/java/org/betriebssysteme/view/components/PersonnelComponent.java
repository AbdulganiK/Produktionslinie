package org.betriebssysteme.view.components;

import com.almasb.fxgl.entity.component.Component;
import org.betriebssysteme.model.personnel.Personnel;

public class PersonnelComponent extends Component {

    private final Personnel person;

    public PersonnelComponent(Personnel person) {
        this.person = person;
    }

    public Personnel getPerson() {
        return person;
    }

}
