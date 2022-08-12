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

    private final Map<UUID, GuiInstance<?>> viewerInstanceMap = new HashMap<>();
    private final Map<Long, GuiInstance<?>> guiInstanceMap = new HashMap<>();
    private final int size;
    private final int id = ++ID_COUNTER;
    private final GuiOptions options;
    private final String title;
    private long next_id = 0;

    public InventoryGui(@NonNull List<GuiPosition.UnsafeGuiPosition> fields) {
        this(null, fields);
    }

    public InventoryGui(String title, List<GuiPosition.UnsafeGuiPosition> fields) {
        this(title, fields, new GuiOptions());
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

    public InventoryGui(List<GuiPosition.UnsafeGuiPosition> fields, GuiOptions options) {
        this(null, fields, options);
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

    public int getId() {
        return id;
    }

    public Set<Player> getViewers() {
        return ImmutableSet.copyOf(viewers);
    }

    @Override
    public @NotNull List<GuiPosition> getParentFields() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull List<GuiPosition> getFields() {
        return fields;
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
    public @Nullable GuiSection getParent() {
        return null;
    }

    @Override
    public @Nullable ItemStack getItem(@NonNull GuiPosition position, InventoryGui.@NonNull GuiInstance<?> instance) {
        Validate.notNull(position);
        Validate.notNull(instance);

        return instance.getInventory().getItem(position.getIndex());
    }

    @Override
    public void setItem(@NonNull GuiPosition position, @NonNull Player player, @NonNull ItemStack item) {
        Validate.notNull(position);
        Validate.notNull(player);

        GuiInstance<?> instance = viewerInstanceMap.get(player.getUniqueId());
        Validate.notNull(instance);

        addComponent(new PersonalItemComponent(position, item, player));
        instance.setItem(position, item);
    }

    @Override
    public void setItem(@NonNull ItemComponent component) {
        Validate.notNull(component);
        addComponent(component.clone(component.getPosition().project(this)));
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

    public Optional<GuiInstance<?>> getInstance(UUID uuid) {
        return Optional.ofNullable(viewerInstanceMap.get(uuid));
    }

    public void addSection(@NonNull GuiSection section) throws InventoryGuiSectionOutOfFields {
        Validate.notNull(section);

        if (getCommonPositions(section).isEmpty()) throw new InventoryGuiSectionOutOfFields();

        guiSections.add(section);
    }

    public boolean close(@NonNull Player player) {
        return close(player, false);
    }

    public boolean close(@NonNull Player player, boolean onEvent) {
        Validate.notNull(player);

        GuiInstance<?> instance = viewerInstanceMap.get(player.getUniqueId());

        if (instance == null) return false;

        InventoryGuiCloseEvent closeEvent = new InventoryGuiCloseEvent(this, instance, player, getOptions().isGuiCleanup());
        Bukkit.getPluginManager().callEvent(closeEvent);

        if (closeEvent.isCancelled()) {
            player.openInventory(instance.getInventory());
            return false;
        }

        instance.setOpen(false);

        player.getPersistentDataContainer().set(INVENTORY_GUI_NAMESPACEKEY, PersistentDataType.INTEGER, -1);

        if (closeEvent.isInstanceRemoved()) {
            removeViewer(player.getUniqueId());
        }

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

    public GuiOptions getOptions() {
        return options;
    }

    public void removeViewer(UUID uuid) {
        if (!viewerInstanceMap.containsKey(uuid)) return;
        GuiInstance<?> instance = viewerInstanceMap.get(uuid);

        if (options.isGuiCleanup() && instance.viewers.contains(uuid) && instance.viewers.size() == 1)
            guiInstanceMap.remove(instance.getId());
    }

    public boolean open(@NonNull Player player) throws UnknownPlayerException, InventoryGuiPlayerLimitException {
        return open(player, Collections.emptyMap());
    }

    public boolean open(@NonNull Player player, Map<String, Object> properties) throws UnknownPlayerException, InventoryGuiPlayerLimitException {
        Validate.notNull(player);
        if (!player.isOnline()) throw new UnknownPlayerException();
        if (getOptions().getPlayerLimit() != -1 && viewers.size() >= getOptions().getPlayerLimit())
            throw new InventoryGuiPlayerLimitException();

        properties = new HashMap<>(properties);
        boolean existedBefore = viewerInstanceMap.containsKey(player.getUniqueId());
        GuiInstance<?> instance;
        if (existedBefore) instance = viewerInstanceMap.get(player.getUniqueId());
        else {

            long id;

            switch (options.getInstanceCreationConfig()) {
                case NEW:
                    //noinspection StatementWithEmptyBody
                    while (guiInstanceMap.containsKey(++next_id)) ;
                    id = next_id;
                    break;
                case WITH_ID:
                    if (properties.containsKey("ID")) {
                        throw new IllegalArgumentException("No id has been provided but it is needed, please provide it in the properties map using key ID");
                    } else if (!(properties.get("ID") instanceof Number)) {
                        throw new IllegalArgumentException("An ID was provided but it's not a number");
                    }

                    id = ((Number) properties.get("ID")).longValue();
                    break;
                default:
                    throw new IllegalArgumentException("InstanceCreationConfig should not be null");
            }

            properties.put("ID", id);
            if(getOptions().isInstanceOwn()) properties.put("OWNER", player.getUniqueId());

            instance = createInstance(id, properties);
        }

        InventoryGuiOpenEvent event = new InventoryGuiOpenEvent(this, instance, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        viewers.add(player);
        player.getPersistentDataContainer().set(INVENTORY_GUI_NAMESPACEKEY, PersistentDataType.INTEGER, id);
        if (!existedBefore) viewerInstanceMap.put(player.getUniqueId(), instance);

        instance.sectionPropertyMap.computeIfAbsent(this, (a) -> new HashMap<>()).putAll(properties);

        instance.updateInventory();
        player.openInventory(instance.inventory);
        instance.setOpen(true);


        return true;
    }

    protected abstract GuiInstance<?> createInstance(long id, Map<String, Object> properties);

    public boolean removeSection(GuiSection section) {
        return guiSections.remove(section);
    }

    public int getSize() {
        return size;
    }

    public GuiInstance<?> getOrCreate(long id) {
        return guiInstanceMap.computeIfAbsent(id, this::createInstance);
    }

    protected GuiInstance<?> createInstance(long id) {
        return createInstance(id, Collections.emptyMap());
    }

    public void removeInstance(long id) {
        guiInstanceMap.remove(id);
    }

    public String getTitle() {
        return title;
    }

    public static abstract class GuiInstance<T extends InventoryGui> {


        private final long id;
        private final UUID owner;

        protected final Set<UUID> viewers = new HashSet<>();
        private final T gui;
        private final Map<GuiSection, Map<String, Object>> sectionPropertyMap = new HashMap<>();
        protected Inventory inventory;
        private boolean isOpen = false;
        private String oldTitle;



        public GuiInstance(@NonNull T gui, long id, Map<String, Object> properties) {

            this.gui = gui;
            this.id = id;
            this.owner = (UUID) Optional.of(properties.get("OWNER")).orElse(null);


            oldTitle = getTitle();
            inventory = createInventory(oldTitle);
        }

        public String getTitle() {
            return getGui().getFromComponent(TitleSupplierComponent.class)
                    .map(s -> ((Function<GuiInstance<?>, String>) s))
                    .orElse(instance -> instance.getGui().getTitle()).apply(this);
        }

        protected abstract Inventory createInventory(String title);

        public T getGui() {
            return gui;
        }

        public Set<UUID> getViewers() {
            return viewers;
        }

        public Optional<UUID> getOwner(){
            return Optional.ofNullable(owner);
        }

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
                        ItemStack itemStack = item.getItem(gui, this);
                        setItem(field.project(gui), itemStack);
                    });
                }
            });
            recUpdate(gui);
        }

        public void update(GuiPosition position) {
            Optional<ItemPanelComponent> opt = gui.getFromComponent(ItemPanelComponent.class);
            opt.flatMap(panelComponent -> panelComponent.getItem(position)).ifPresent(item -> {
                ItemStack itemStack = item.getItem(gui, this);
                if (itemStack == null) return;
                setItem(position.project(gui), itemStack);
            });
        }

        public void setItem(GuiPosition position, ItemStack stack) {
            getInventory().setItem(position.getIndex(), stack);
        }

        public Inventory getInventory() {
            return inventory;
        }

        private void recUpdate(GuiSection section) {
            for (GuiSection subSection : section.getSubSections()) {
                subSection.getFromComponent(ItemPanelComponent.class).ifPresent(panelComponent -> {
                    for (Map.Entry<GuiPosition, ItemComponent> entry : panelComponent.getItemComponentMap().entrySet()) {
                        ItemStack item = entry.getValue().getItem(getGui(), this);
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

        public boolean isOpen() {
            return isOpen;
        }

        protected void setOpen(boolean open) {
            isOpen = open;
        }

        public long getId() {
            return id;
        }
    }
}
