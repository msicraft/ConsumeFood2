package me.msicraft.consumefood2.CustomFood.Event;

import me.msicraft.consumefood2.ConsumeFood2;
import me.msicraft.consumefood2.CustomFood.CustomFoodManager;
import org.bukkit.event.Listener;

public class CustomFoodRelatedEvent implements Listener {

    private final ConsumeFood2 plugin;
    private final CustomFoodManager customFoodManager;

    public CustomFoodRelatedEvent(ConsumeFood2 plugin) {
        this.plugin = plugin;
        this.customFoodManager = plugin.getCustomFoodManager();
    }

}
