package fr.wyrdix.inventory;

import fr.wyrdix.inventory.component.ItemComponent;
import fr.wyrdix.inventory.section.GuiSection;
import org.apache.commons.lang.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.function.Function;


public final class GuiLayout {

    private static final Function<GuiPosition, ItemComponent> AIR_CREATOR = (position -> new ItemComponent(position, null));

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

            section.setItem(itemComponentMap.getOrDefault(c, AIR_CREATOR).apply(field));
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
