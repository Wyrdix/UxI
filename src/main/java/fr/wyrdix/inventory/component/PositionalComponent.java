package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.GuiPosition;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface PositionalComponent extends Component {

    @NonNull GuiPosition getPosition();

}
