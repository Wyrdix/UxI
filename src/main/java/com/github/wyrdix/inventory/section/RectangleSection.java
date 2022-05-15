package com.github.wyrdix.inventory.section;

import com.github.wyrdix.inventory.GuiPosition;
import org.apache.commons.lang.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

public class RectangleSection extends SimpleGuiSection {

    public RectangleSection(@NonNull GuiSection parent, @NonNull GuiPosition corner1, @NonNull GuiPosition corner2) {
        super(parent);
        Validate.notNull(parent);
        Validate.notNull(corner1);
        Validate.notNull(corner2);

        Validate.isTrue(corner1.getX() <= corner2.getX());
        Validate.isTrue(corner1.getY() <= corner2.getY());

        int size = parent.getFields().size();
        for (int i = 0; i < size; i++) {
            GuiPosition position = parent.getFields().get(i);

            if (position.getX() < corner1.getX() || position.getX() > corner2.getX()) continue;
            if (position.getY() < corner1.getY() || position.getY() > corner2.getY()) continue;

            parentFields.add(position);
            fields.add(new GuiPosition(this, fields.size(), position.getX() - corner1.getX(), position.getY() - corner1.getY()));
        }
    }

    @Override
    public String toString() {
        return "RectangleSection{" +
                "fields=" + fields +
                ", parentFields=" + parentFields +
                ", sections=" + getSubSections() +
                ", components=" + getComponents() +
                ", parent=" + getParent() +
                '}';
    }
}
