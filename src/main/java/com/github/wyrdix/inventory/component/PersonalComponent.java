package com.github.wyrdix.inventory.component;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface PersonalComponent extends Component {

    @NonNull Player getPlayer();

}
