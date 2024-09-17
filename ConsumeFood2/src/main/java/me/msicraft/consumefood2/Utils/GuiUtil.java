package me.msicraft.consumefood2.Utils;

import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;

public class GuiUtil {

    private GuiUtil() {}

    public static final ItemStack AIR_STACK = new ItemStack(Material.AIR, 1);
    public static final List<String> EMPTY_LORE = Collections.emptyList();

    public static ItemStack createItemStack(Material material, String name, List<String> list, int customModelData, String dataTag, String data) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(list);
        if (customModelData != -1) {
            itemMeta.setCustomModelData(customModelData);
        }
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(new NamespacedKey(ConsumeFood2.getPlugin(), dataTag), PersistentDataType.STRING, data);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(Material material, String name, List<String> list, int customModelData, String dataTag, String data, ItemFlag[] flags) {
        ItemStack itemStack = createItemStack(material, name, list, customModelData, dataTag, data);
        ItemMeta itemMeta = itemStack.getItemMeta();
        for (ItemFlag flag : flags) {
            itemMeta.addItemFlags(flag);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(Material material, String name, List<String> list, int customModelData, NamespacedKey key, String data) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(list);
        if (customModelData != -1) {
            itemMeta.setCustomModelData(customModelData);
        }
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(key, PersistentDataType.STRING, data);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(Material material, String name, List<String> list, int customModelData, NamespacedKey key, String data, ItemFlag[] flags) {
        ItemStack itemStack = createItemStack(material, name, list, customModelData, key, data);
        ItemMeta itemMeta = itemStack.getItemMeta();
        for (ItemFlag flag : flags) {
            itemMeta.addItemFlags(flag);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
