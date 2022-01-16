package fr.wyrdix.inventory.component;

import fr.wyrdix.inventory.GuiPosition;
import fr.wyrdix.inventory.InventoryGui;
import fr.wyrdix.inventory.event.InventoryGuiClickEvent;
import fr.wyrdix.inventory.section.GuiSection;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ItemComponent implements PositionalComponent {

    private final GuiPosition position;
    private final ItemStack stack;
    private final ItemMeta meta;
    private final ClickingRunnable clickingRunnable;
    private final CreateRunnable createRunnable;

    public ItemComponent(@NonNull GuiPosition position, @Nullable ItemStack stack) {
        this(position, stack, (event, section, player, pos) -> {
        }, (gui, player, itemStack, instance) -> itemStack);
    }

    public ItemComponent(@NonNull GuiPosition position, @Nullable ItemStack stack, @NonNull ClickingRunnable clickingRunnable, @NonNull CreateRunnable createRunnable) {
        Validate.notNull(position);
        Validate.notNull(clickingRunnable);
        Validate.notNull(createRunnable);

        this.position = position;
        this.stack = stack == null ? new ItemStack(Material.AIR) : stack;
        this.meta = this.stack.getItemMeta();
        this.clickingRunnable = clickingRunnable;
        this.createRunnable = createRunnable;
    }

    private ItemComponent(@NonNull GuiPosition position, @NonNull ItemStack stack, @NonNull ItemMeta meta, @NonNull ClickingRunnable clickingRunnable, @NonNull CreateRunnable createRunnable) {
        Validate.notNull(position);
        Validate.notNull(stack);
        Validate.notNull(clickingRunnable);
        Validate.notNull(createRunnable);

        this.position = position;
        this.stack = stack;
        this.meta = meta;
        this.clickingRunnable = clickingRunnable;
        this.createRunnable = createRunnable;
    }

    public ItemMeta getMeta() {
        return meta;
    }

    public ItemStack getItem(InventoryGui gui, Player player) {
        stack.setItemMeta(meta);
        Optional<InventoryGui.GuiInstance<?>> opt = gui.getInstance(player.getUniqueId());
        if (opt.isEmpty()) return null;
        return createRunnable.onCreate(gui, player, stack, opt.get());
    }

    @SuppressWarnings("unused")
    public void onClick(InventoryGuiClickEvent event, GuiSection section, Player player) {
        clickingRunnable.onClick(event, section, player, position);
    }

    @Override
    public @NonNull GuiPosition getPosition() {
        return position;
    }

    public ItemComponent clone(GuiPosition position) {
        return new ItemComponent(position, stack.clone(), meta, clickingRunnable, createRunnable);
    }

    @SuppressWarnings("deprecation")
    public ItemComponent name(@Nullable String string) {
        if (string == null) meta.setDisplayName(null);
        else meta.setDisplayName("§r§f" + string);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemComponent name() {
        meta.setDisplayName("§r");
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemComponent lore(@NonNull List<String> lines) {
        Validate.notNull(lines);

        meta.setLore(lines.stream().map(s -> "§r" + s).toList());
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemComponent lore() {
        meta.setLore(Collections.emptyList());
        return this;
    }

    public ItemComponent enchant(@NonNull Enchantment enchantment, int level) {
        Validate.notNull(enchantment);

        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemComponent enchant(@NonNull Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        Validate.notNull(enchantment);

        meta.addEnchant(enchantment, level, ignoreLevelRestriction);
        return this;
    }

    public ItemComponent attribute(Attribute attribute, AttributeModifier modifier) {
        Validate.notNull(attribute);
        Validate.notNull(modifier);


        meta.addAttributeModifier(attribute, modifier);
        return this;
    }

    public ItemComponent modelData(int data) {
        meta.setCustomModelData(data);
        return this;
    }

    public ItemComponent flags(@NonNull ItemFlag... flags) {
        Validate.notNull(flags);

        meta.addItemFlags(flags);
        return this;
    }

    public interface ClickingRunnable {
        void onClick(@NonNull InventoryGuiClickEvent event, @NonNull GuiSection section, @NonNull Player player, @NonNull GuiPosition position);
    }

    public interface CreateRunnable {
        ItemStack onCreate(@NonNull InventoryGui gui, @NonNull Player player, @NonNull ItemStack stack, InventoryGui.@NonNull GuiInstance<?> instance);
    }
}
