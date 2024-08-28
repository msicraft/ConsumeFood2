package me.msicraft.upper_1_20_6;

import me.msicraft.wrapper.Wrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Upper_1_20_6 implements Wrapper {

    private static Upper_1_20_6 instance;

    private Upper_1_20_6() {
    }

    public static Upper_1_20_6 getInstance() {
        if (instance == null) {
            instance = new Upper_1_20_6();
        }
        return instance;
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Material.APPLE);
    }

}
