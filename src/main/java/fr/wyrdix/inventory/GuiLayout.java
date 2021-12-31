package fr.wyrdix.inventory;

import fr.wyrdix.inventory.component.ItemComponent;
import fr.wyrdix.inventory.section.GuiSection;
import org.apache.commons.lang.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public final class GuiLayout {

    private GuiLayout() {
    }

    public static void fromStringLayout(@NonNull GuiSection section,
                                        @NonNull String[] layout,
                                        @NonNull Map<Character, ItemComponent> itemComponentMap) {
        Validate.notNull(section);
        Validate.notNull(layout);
        Validate.notNull(itemComponentMap);


        Map<Character, Function<GuiPosition, ItemComponent>> toFunction = new HashMap<>();

        for (Map.Entry<Character, ItemComponent> entry : itemComponentMap.entrySet()) {
            toFunction.put(entry.getKey(), (position -> entry.getValue().clone(position)));
        }

        fromStringLayoutWithFunction(section, layout, toFunction);
    }

    public static void fromStringLayoutWithFunction(@NonNull GuiSection section,
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

            section.setItem(itemComponentMap.get(c).apply(field));
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
