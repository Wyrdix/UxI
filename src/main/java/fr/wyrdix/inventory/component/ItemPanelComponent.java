package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.section.GuiSection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemPanelComponent implements Component {
    private final GuiSection section;
    private final Map<GuiPosition, ItemComponent> itemComponentMap = new HashMap<>();

    public ItemPanelComponent(GuiSection section) {
        this.section = section;
    }

    public GuiSection getSection() {
        return section;
    }

    @Override
    public void onAddition(@NonNull GuiSection section) {
        for (GuiPosition field : section.getFields()) {
            itemComponentMap.put(field, new ItemComponent(field, null));
        }
    }

    public @Nullable ItemComponent getItem(GuiPosition position) {
        return itemComponentMap.get(position);
    }

    public void setItem(ItemComponent itemComponent) {
        itemComponentMap.put(itemComponent.getPosition(), itemComponent);
    }
}
