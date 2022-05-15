package com.github.wyrdix.inventory.component;

import com.github.wyrdix.inventory.GuiPosition;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface PositionalComponent extends Component {

    @NonNull GuiPosition getPosition();

}
