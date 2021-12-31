package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.section.GuiSection;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Component {

    default void onAddition(@NonNull GuiSection section) {
    }

    default void onDeletion(@NonNull GuiSection section) {
    }
}