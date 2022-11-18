package com.github.wyrdix.inventory.section;

import com.github.wyrdix.inventory.GuiPosition;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class SlotFreeSection extends SlotSection {
    public SlotFreeSection(@NonNull GuiSection parent, @NonNull GuiPosition position) {
        super(parent, position);
    }

    public SlotFreeSection(GuiSection parent, int slot) {
        super(parent, slot);
    }

    @Override
    public boolean isFree() {
        return true;
    }
}
