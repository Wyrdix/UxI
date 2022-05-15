package com.github.wyrdix.inventory;

import com.github.wyrdix.UxiPlugin;
import com.github.wyrdix.inventory.component.*;
import com.github.wyrdix.inventory.event.InventoryGuiCloseEvent;
import com.github.wyrdix.inventory.event.InventoryGuiComponentAddEvent;
import com.github.wyrdix.inventory.event.InventoryGuiComponentRemoveEvent;
import com.github.wyrdix.inventory.event.InventoryGuiOpenEvent;
import com.github.wyrdix.inventory.exceptions.InventoryGuiPlayerLimitException;
import com.github.wyrdix.inventory.exceptions.InventoryGuiSectionOutOfFields;
import com.github.wyrdix.inventory.exceptions.UnknownPlayerException;
import com.github.wyrdix.inventory.section.GuiSection;
import com.github.wyrdix.inventory.section.SimpleGuiSection;
import com.github.wyrdix.inventory.section.SlotSection;
import com.google.common.collect.ImmutableSet;
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
import java.util.function.Function;

public abstract class InventoryGui extends SimpleGuiSection {

    static final Map<Integer, InventoryGui> INVENTORY_GUIS = new HashMap<>();
    private static final NamespacedKey INVENTORY_GUI_NAMESPACEKEY = new NamespacedKey(UxiPlugin.getInstance(), "gui_inventory_id");
    private static int ID_COUNTER = 0;
    private final Set<Player> viewers = new HashSet<>();
    private final List<GuiPosition> fields;
    private final Set<Component> components = new HashSet<>();
    private final Set<GuiSection> guiSections = new HashSet<>();
    private final Map<UUID, GuiInstance<?>> guiInstanceMap = new HashMap<>();
    private final int size;
    private final int id = ++ID_COUNTER;
    private final GuiOptions options;
    private final String title;

    public InventoryGui(@NonNull List<GuiPosition.UnsafeGuiPosition> fields) {
        this(null, fields);
    }

    public InventoryGui(List<GuiPosition.UnsafeGuiPosition> fields, GuiOptions options) {
        this(null, fields, options);
    }

    public InventoryGui(String title, List<GuiPosition.UnsafeGuiPosition> fields, GuiOptions options) {
        super(null);
        this.title = title;
        Validate.notNull(fields);
        this.fields = new ArrayList<>();
        fields.forEach(s -> {
            GuiPosition position = s.toGuiPosition(this);
            this.fields.add(position);
            this.guiSections.add(new SlotSection(this, position.getIndex()));
        });

        size = this.fields.size();
        this.options = options;

        if (options.getGuiRefreshRate() > 0) InventoryGuiUpdater.INVENTORY_GUI.put(id, System.currentTimeMillis());
        INVENTORY_GUIS.put(id, this);
    }

    public InventoryGui(String title, List<GuiPosition.UnsafeGuiPosition> fields) {
        this(title, fields, new GuiOptions());
    }

    protected static GuiPosition createUnsafePosition(@NonNull InventoryGui gui, int index, int x, int y) {
        Validate.notNull(gui);
        return new GuiPosition(gui, index, x, y);
    }

    public static Optional<InventoryGui> getOpenedInventory(@NonNull Player player) {
        Validate.notNull(player);
        Validate.isTrue(player.isOnline());

        Integer id = player.getPersistentDataContainer().get(InventoryGui.INVENTORY_GUI_NAMESPACEKEY, PersistentDataType.INTEGER);
        if (id == null) return Optional.empty();
        InventoryGui gui = InventoryGui.INVENTORY_GUIS.get(id);
        return Optional.ofNullable(gui);
    }

    public Set<Player> getViewers() {
        return ImmutableSet.copyOf(viewers);
    }

    @Override
    public @Nullable GuiSection getParent() {
        return null;
    }

    @Override
    public @NotNull List<GuiPosition> getParentFields() {
        return fields;
    }

    public boolean close(@NonNull Player player) {
        return close(player, false);
    }

    public boolean open(@NonNull Player player) throws UnknownPlayerException, InventoryGuiPlayerLimitException {
        return open(player, Collections.emptyMap());
    }

    public boolean open(@NonNull Player player, Map<String, Object> properties) throws UnknownPlayerException, InventoryGuiPlayerLimitException {
        Validate.notNull(player);
        if (!player.isOnline()) throw new UnknownPlayerException();
        if (getOptions().getPlayerLimit() != -1 && viewers.size() >= getOptions().getPlayerLimit())
            throw new InventoryGuiPlayerLimitException();

        InventoryGuiOpenEvent event = new InventoryGuiOpenEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        viewers.add(player);
        player.getPersistentDataContainer().set(INVENTORY_GUI_NAMESPACEKEY, PersistentDataType.INTEGER, id);
        GuiInstance<?> instance = guiInstanceMap.computeIfAbsent(player.getUniqueId(), this::createInstance);

        instance.sectionPropertyMap.computeIfAbsent(this, (a) -> new HashMap<>()).putAll(properties);

        instance.updateInventory();
        player.openInventory(instance.inventory);
        instance.setOpen(true);


        return true;
    }


    public boolean addComponent(@NonNull Component component) {
        Validate.notNull(component);

        InventoryGuiComponentAddEvent event = new InventoryGuiComponentAddEvent(this, component);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) components.add(component);
        return !event.isCancelled();
    }

    public boolean removeComponent(@NonNull Component component) {
        Validate.notNull(component);

        InventoryGuiComponentRemoveEvent event = new InventoryGuiComponentRemoveEvent(this, component);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) components.remove(component);
        return !event.isCancelled();
    }

    public void addSection(@NonNull GuiSection section) throws InventoryGuiSectionOutOfFields {
        Validate.notNull(section);

        if (getCommonPositions(section).isEmpty()) throw new InventoryGuiSectionOutOfFields();

        guiSections.add(section);
    }

    public boolean close(@NonNull Player player, boolean onEvent) {
        Validate.notNull(player);

        GuiInstance<?> instance = guiInstanceMap.get(player.getUniqueId());

        if (instance == null) return false;

        InventoryGuiCloseEvent closeEvent = new InventoryGuiCloseEvent(this, player, getOptions().isGuiCleanup());
        Bukkit.getPluginManager().callEvent(closeEvent);

        if (closeEvent.isCancelled()) {
            player.openInventory(instance.getInventory());
            return false;
        }

        instance.setOpen(false);

        player.getPersistentDataContainer().set(INVENTORY_GUI_NAMESPACEKEY, PersistentDataType.INTEGER, -1);

        if (closeEvent.isInstanceRemoved()) guiInstanceMap.remove(player.getUniqueId());

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
    public void setItem(@NonNull GuiPosition position, @NonNull Player player, @NonNull ItemStack item) {
        Validate.notNull(position);
        Validate.notNull(player);

        GuiInstance<?> instance = guiInstanceMap.get(player.getUniqueId());
        Validate.notNull(instance);

        addComponent(new PersonalItemComponent(position, item, player));
        instance.setItem(position, item);
    }

    @Override
    public void setItem(@NonNull ItemComponent component) {
        Validate.notNull(component);
        addComponent(component.clone(component.getPosition().project(this)));
    }

    protected abstract GuiInstance<?> createInstance(UUID uuid);

    public Optional<GuiInstance<?>> getInstance(UUID uuid) {
        return Optional.ofNullable(guiInstanceMap.get(uuid));
    }

    public GuiInstance<?> getOrCreate(UUID uuid) {
        return guiInstanceMap.computeIfAbsent(uuid, this::createInstance);
    }

    public void removeInstance(UUID uuid) {
        guiInstanceMap.remove(uuid);
    }

    public String getTitle() {
        return title;
    }

    public static abstract class GuiInstance<T extends InventoryGui> {

        private final T gui;
        private final UUID owner;
        private final Map<GuiSection, Map<String, Object>> sectionPropertyMap = new HashMap<>();
        protected Inventory inventory;
        private boolean isOpen = false;

        private String oldTitle;

        public GuiInstance(@NonNull T gui, @NonNull UUID owner) {

            this.gui = gui;
            this.owner = owner;

            oldTitle = getTitle();
            inventory = createInventory(oldTitle);
        }

        public T getGui() {
            return gui;
        }

        public UUID getOwner() {
            return owner;
        }

        public String getTitle() {
            return getGui().getFromComponent(TitleSupplierComponent.class)
                    .map(s -> ((Function<GuiInstance<?>, String>) s))
                    .orElse(instance -> instance.getGui().getTitle()).apply(this);
        }

        protected abstract Inventory createInventory(String title);


        public void updateInventory() {
            if (oldTitle != null) {
                String title = getTitle();
                if (!oldTitle.equals(title)) {
                    oldTitle = title;
                    inventory = createInventory(oldTitle);
                }
            }
            Optional<ItemPanelComponent> opt = gui.getFromComponent(ItemPanelComponent.class);
            opt.ifPresent(panelComponent -> {
                for (GuiPosition field : gui.getFields()) {
                    panelComponent.getItem(field).ifPresent(item -> {
                        ItemStack itemStack = item.getItem(gui, Objects.requireNonNull(Bukkit.getPlayer(owner)));
                        if (itemStack == null) return;
                        setItem(field.project(gui), itemStack);
                    });
                }
            });
            recUpdate(gui);
        }

        public void update(GuiPosition position) {
            Optional<ItemPanelComponent> opt = gui.getFromComponent(ItemPanelComponent.class);
            opt.flatMap(panelComponent -> panelComponent.getItem(position)).ifPresent(item -> {
                ItemStack itemStack = item.getItem(gui, Objects.requireNonNull(Bukkit.getPlayer(owner)));
                if (itemStack == null) return;
                setItem(position.project(gui), itemStack);
            });
        }

        private void recUpdate(GuiSection section) {
            for (GuiSection subSection : section.getSubSections()) {
                subSection.getFromComponent(ItemPanelComponent.class).ifPresent(panelComponent -> {
                    for (Map.Entry<GuiPosition, ItemComponent> entry : panelComponent.getItemComponentMap().entrySet()) {
                        ItemStack item = entry.getValue().getItem(getGui(), Objects.requireNonNull(Bukkit.getPlayer(getOwner())));
                        if (item == null) continue;
                        setItem(entry.getKey().project(getGui()), item);
                    }
                });
                recUpdate(subSection);
            }
        }

        @SuppressWarnings("unchecked")
        public <U> Optional<U> get(GuiSection section, String key) {
            return Optional.ofNullable(((U) sectionPropertyMap.getOrDefault(section, Collections.emptyMap()).getOrDefault(key, null)));
        }

        public void set(GuiSection section, String key, Object object) {
            Map<String, Object> map = sectionPropertyMap.computeIfAbsent(section, s -> new HashMap<>());
            map.put(key, object);
        }

        public Inventory getInventory() {
            return inventory;
        }

        public boolean isOpen() {
            return isOpen;
        }

        protected void setOpen(boolean open) {
            isOpen = open;
        }

        public void setItem(GuiPosition position, ItemStack stack) {
            getInventory().setItem(position.getIndex(), stack);
        }
    }
}
