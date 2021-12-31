package fr.wyrdix.inventory;

import com.google.common.collect.ImmutableSet;
import fr.wyrdix.UxiPlugin;
import fr.wyrdix.inventory.component.Component;
import fr.wyrdix.inventory.component.ItemComponent;
import fr.wyrdix.inventory.component.PersonalComponent;
import fr.wyrdix.inventory.component.PersonalItemComponent;
import fr.wyrdix.inventory.event.InventoryGuiCloseEvent;
import fr.wyrdix.inventory.event.InventoryGuiComponentAddEvent;
import fr.wyrdix.inventory.event.InventoryGuiComponentRemoveEvent;
import fr.wyrdix.inventory.event.InventoryGuiOpenEvent;
import fr.wyrdix.inventory.exceptions.InventoryGuiPlayerLimitException;
import fr.wyrdix.inventory.exceptions.InventoryGuiSectionOutOfFields;
import fr.wyrdix.inventory.exceptions.UnknownPlayerException;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class InventoryGui implements GuiSection {

    static final Map<Integer, InventoryGui> INVENTORY_GUIS = new HashMap<>();
    static final NamespacedKey INVENTORY_GUI_NAMESPACEKEY = new NamespacedKey(UxiPlugin.getInstance(), "gui_inventory_id");

    private static int ID_COUNTER = 0;


    private final Set<Player> viewers = new HashSet<>();
    private final List<GuiPosition> fields;
    private final Set<Component> components = new HashSet<>();
    private final Set<GuiSection> guiSections = new HashSet<>();
    private final Map<GuiPosition, List<ItemComponent>> itemComponentMap = new HashMap<>();

    private final Map<UUID, GuiInstance<?>> guiInstanceMap = new HashMap<>();

    private final int size;
    private final int id = ++ID_COUNTER;

    private final GuiOptions options;

    public InventoryGui(@NonNull List<GuiPosition.UnsafeGuiPosition> fields) {
        this(fields, new GuiOptions());
    }

    public InventoryGui(List<GuiPosition.UnsafeGuiPosition> fields, GuiOptions options) {
        Validate.notNull(fields);
        this.fields = new ArrayList<>();
        fields.forEach(s -> {
            GuiPosition position = s.toGuiPosition(this);
            this.fields.add(position);
            this.guiSections.add(new SlotSection(this, position.getIndex()));
            itemComponentMap.put(position, new ArrayList<>());
        });

        size = this.fields.size();
        this.options = options;

        INVENTORY_GUIS.put(id, this);
    }

    protected static GuiPosition createUnsafePosition(@NonNull InventoryGui gui, int index, int x, int y) {
        Validate.notNull(gui);
        return new GuiPosition(gui, index, x, y);
    }

    @Override
    public @Nullable GuiSection getParent() {
        return null;
    }

    @Override
    public @Nullable List<GuiPosition> getParentFields() {
        return fields;
    }

    public boolean open(@NonNull Player player) throws UnknownPlayerException, InventoryGuiPlayerLimitException {
        Validate.notNull(player);
        if (!player.isOnline()) throw new UnknownPlayerException();
        if (getOptions().getPlayerLimit() != -1 && viewers.size() >= getOptions().getPlayerLimit())
            throw new InventoryGuiPlayerLimitException();

        InventoryGuiOpenEvent event = new InventoryGuiOpenEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        viewers.add(player);

        GuiInstance<?> instance = guiInstanceMap.computeIfAbsent(player.getUniqueId(), this::createInstance);
        player.openInventory(instance.inventory);
        instance.setOpen(true);

        player.getPersistentDataContainer().set(INVENTORY_GUI_NAMESPACEKEY, PersistentDataType.INTEGER, id);

        return true;
    }

    public boolean close(@NonNull Player player) {
        return close(player, false);
    }

    public boolean close(@NonNull Player player, boolean onEvent) {
        Validate.notNull(player);

        GuiInstance<?> instance = guiInstanceMap.get(player.getUniqueId());

        if (instance == null) return false;

        InventoryGuiCloseEvent closeEvent = new InventoryGuiCloseEvent(this, player);
        Bukkit.getPluginManager().callEvent(closeEvent);

        if (closeEvent.isCancelled()) {
            player.openInventory(instance.getInventory());
            return false;
        }

        instance.setOpen(false);

        player.getPersistentDataContainer().set(INVENTORY_GUI_NAMESPACEKEY, PersistentDataType.INTEGER, -1);

        if (getOptions().isGuiCleanup()) guiInstanceMap.remove(player.getUniqueId());

        if (onEvent || !player.getOpenInventory().getTopInventory().equals(instance.getInventory())) {
            viewers.remove(player);
            return false;
        }

        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);

        components.forEach(s -> {
            if (s instanceof PersonalComponent && ((PersonalComponent) s).getPlayer().getUniqueId().equals(player.getUniqueId()))
                removeComponent(s);
        });

        return viewers.remove(player);
    }

    public void addComponent(Component component) {
        Validate.notNull(component);

        InventoryGuiComponentAddEvent event = new InventoryGuiComponentAddEvent(this, component);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            components.add(component);
            if (component instanceof ItemComponent itemComponent) {
                itemComponentMap.get(itemComponent.getPosition()).add(itemComponent);
            }
        }
    }

    public void removeComponent(@NonNull Component component) {
        Validate.notNull(component);

        InventoryGuiComponentRemoveEvent event = new InventoryGuiComponentRemoveEvent(this, component);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            components.remove(component);
            if (component instanceof ItemComponent itemComponent) {
                itemComponentMap.get(itemComponent.getPosition()).remove(component);
            }
        }
    }

    public void addSection(@NonNull GuiSection section) throws InventoryGuiSectionOutOfFields {
        Validate.notNull(section);
        if (!isDistinct(section)) throw new InventoryGuiSectionOutOfFields();

        guiSections.add(section);
    }

    public boolean removeSection(GuiSection section) {
        return guiSections.remove(section);
    }

    @Override
    public @NonNull Set<GuiSection> getSubSections() {
        return ImmutableSet.copyOf(guiSections);
    }

    @Override
    public @NonNull Set<Component> getComponents() {
        return ImmutableSet.copyOf(components);
    }

    @Override
    public @NonNull List<GuiPosition> getFields() {
        return fields;
    }

    public int getSize() {
        return size;
    }

    public GuiOptions getOptions() {
        return options;
    }

    @Override
    public @Nullable ItemStack getItem(@NonNull GuiPosition position, @NonNull Player player) {
        Validate.notNull(position);
        Validate.notNull(player);

        GuiInstance<?> instance = guiInstanceMap.get(player.getUniqueId());
        Validate.notNull(instance);

        return instance.getInventory().getItem(position.getIndex());
    }

    @Override
    public void setItem(@NotNull GuiPosition position, @NotNull Player player, @NotNull ItemStack item) {
        Validate.notNull(position);
        Validate.notNull(player);

        GuiInstance<?> instance = guiInstanceMap.get(player.getUniqueId());
        Validate.notNull(instance);

        addComponent(new PersonalItemComponent(position, item, player));
        instance.getInventory().setItem(position.getIndex(), item);
    }

    protected abstract GuiInstance<?> createInstance(UUID uuid);

    protected static abstract class GuiInstance<T extends InventoryGui> {

        private final T gui;
        private final UUID owner;
        private final Inventory inventory;

        private boolean isOpen = false;

        public GuiInstance(@NonNull T gui, @NonNull UUID owner) {

            this.gui = gui;
            this.owner = owner;

            inventory = createInventory();
        }

        public T getGui() {
            return gui;
        }

        public UUID getOwner() {
            return owner;
        }

        protected abstract Inventory createInventory();

        public Inventory getInventory() {
            return inventory;
        }

        public boolean isOpen() {
            return isOpen;
        }

        protected void setOpen(boolean open) {
            isOpen = open;
        }
    }
}