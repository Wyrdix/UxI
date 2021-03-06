package com.github.wyrdix.inventory;

import com.github.wyrdix.inventory.component.ItemComponent;
import com.github.wyrdix.inventory.section.GuiSection;
import org.apache.commons.lang.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


public final class GuiLayout {

    private GuiLayout() {
    }

    public static void fromStringLayout(@NonNull GuiSection section,
                                        @NonNull String[] layout,
                                        @NonNull Map<Character, Function<GuiPosition, ItemComponent>> itemComponentMap) {
        Validate.notNull(section);
        Validate.notNull(layout);
        Validate.notNull(itemComponentMap);

        for (GuiPosition field : section.getFields()) {

            if (layout.length < field.getY()) throw new IllegalArgumentException("A line is missing in the layout");
            String line = layout[field.getY()];
            if (line.length() < field.getX()) throw new IllegalArgumentException("A line isn't long enough");

            char c = line.charAt(field.getX());

            Optional.ofNullable(itemComponentMap.get(c)).ifPresent(s -> section.setItem(s.apply(field)));
        }
    }

    public static void fillLayout(@NonNull GuiSection section, @NonNull Function<GuiPosition, ItemComponent> itemCreator) {
        Validate.notNull(section);
        Validate.notNull(itemCreator);

        for (GuiPosition field : section.getFields()) {

            section.setItem(itemCreator.apply(field));
        }

    }
}
