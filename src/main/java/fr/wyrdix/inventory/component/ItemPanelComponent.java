package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.section.GuiSection;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemPanelComponent implements Component {
    private final GuiSection section;
    private final Map<GuiPosition, ItemComponent> itemComponentMap = new HashMap<>();

    public ItemPanelComponent(GuiSection section) {
        this.section = section;
    }

    public GuiSection getSection() {
        return section;
    }

    public @NonNull Optional<ItemComponent> getItem(GuiPosition position) {
        return Optional.ofNullable(itemComponentMap.get(position));
    }

    public void setItem(ItemComponent itemComponent) {
        itemComponentMap.put(itemComponent.getPosition(), itemComponent);
    }

    public Map<GuiPosition, ItemComponent> getItemComponentMap() {
        return itemComponentMap;
    }
}
