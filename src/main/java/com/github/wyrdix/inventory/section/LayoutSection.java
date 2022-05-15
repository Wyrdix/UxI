package com.github.wyrdix.inventory.section;

import com.github.wyrdix.inventory.GuiPosition;
import org.apache.commons.lang.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

public class LayoutSection extends SimpleGuiSection {

    public LayoutSection(@NonNull GuiSection parent, String[] layout, char match) {
        super(parent);
        Validate.notNull(parent);
        Validate.notNull(layout);

        int height = layout.length;
        Validate.isTrue(height > 0);

        int size = parent.getFields().size();
        for (int i = 0; i < size; i++) {
            GuiPosition position = parent.getFields().get(i);

            char c = layout[position.getY()].charAt(position.getX());
            if (c != match) continue;
            parentFields.add(position);
            fields.add(new GuiPosition(this, fields.size(), position.getX(), position.getY()));
        }
    }

    @Override
    public String toString() {
        return "LayoutSection{" +
                "fields=" + fields +
                ", parentFields=" + parentFields +
                ", sections=" + getSubSections() +
                ", components=" + getComponents() +
                ", parent=" + getParent() +
                '}';
    }
}
