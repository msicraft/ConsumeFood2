package me.msicraft.API.Food;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

public class CustomFood extends Food {

    private final String internalName;
    private final List<String> lore = new ArrayList<>();
    private final Map<Enchantment, Integer> enchantMap = new HashMap<>();

    public CustomFood(Material material, String internalName) {
        super(material);
        this.internalName = internalName;
    }

    public void addLore(String loreLine) {
        lore.add(loreLine);
    }

    public void clearLore() {
        lore.clear();
    }

    public void addEnchantment(Enchantment enchantment, int level) {
        enchantMap.put(enchantment, level);
    }

    public void removeEnchantment(Enchantment enchantment) {
        enchantMap.remove(enchantment);
    }

    public void clearEnchant() {
        enchantMap.clear();
    }

    public String getInternalName() {
        return internalName;
    }

    public List<String> getLore() {
        return lore;
    }

    public Set<Enchantment> getEnchantments() {
        return enchantMap.keySet();
    }

    public int getEnchantmentLevel(Enchantment enchantment) {
        return enchantMap.getOrDefault(enchantment, 0);
    }

    public List<String> getEnchantFormatList() {
        List<String> list = new ArrayList<>();
        for (Enchantment enchantment : enchantMap.keySet()) {
            list.add(enchantment.getKey().getKey() + ":" + getEnchantmentLevel(enchantment));
        }
        return list;
    }

}
