package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.GuiPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PersonalItemComponent extends ItemComponent implements PersonalComponent {
    private final Player player;

    public PersonalItemComponent(GuiPosition position, ItemStack stack, Player player) {
        super(position, stack);
        this.player = player;
    }

    @Override
    public @NonNull Player getPlayer() {
        return player;
    }
}
