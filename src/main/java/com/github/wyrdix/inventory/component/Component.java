package com.github.wyrdix.inventory.component;

import com.github.wyrdix.inventory.section.GuiSection;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Component {

    default void onAddition(@NonNull GuiSection section) {
    }

    default void onDeletion(@NonNull GuiSection section) {
    }
}