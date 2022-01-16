package fr.wyrdix.inventory.section;

import fr.wyrdix.inventory.GuiPosition;

public class SlotSection extends SimpleGuiSection {
    private final int slot;

    public SlotSection(GuiSection parent, int slot) {
        super(parent);

        this.slot = slot;
        fields.add(new GuiPosition(this, 0, 0, 0));
        parentFields.add(parent.getFields().get(slot));
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public String toString() {
        return "SlotSection{" +
                "fields=" + fields +
                ", parentFields=" + parentFields +
                ", components=" + getComponents() +
                ", parent=" + getParent() +
                ", slot=" + slot +
                '}';
    }
}
