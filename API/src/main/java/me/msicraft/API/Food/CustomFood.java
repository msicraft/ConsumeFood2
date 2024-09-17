package me.msicraft.API.Food;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import me.msicraft.API.ConsumeFood2API;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

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

    public ItemStack getGuiItemStack(boolean upper) {
        ItemStack itemStack = new ItemStack(getMaterial());
        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = (String) getOptionValue(Options.DISPLAYNAME);
        if (displayName != null) {
            itemMeta.setDisplayName(ConsumeFood2API.translateColorCodes(displayName));
        }

        itemStack.setItemMeta(itemMeta);
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            if (upper) {
                NBT.modifyComponents(itemStack, nbt -> {
                    ReadWriteNBT profileNbt = nbt.getOrCreateCompound("minecraft:profile");
                    profileNbt.setUUID("id", (UUID) getOptionValue(Food.Options.UUID));
                    ReadWriteNBT propertiesNbt = profileNbt.getCompoundList("properties").addCompound();
                    propertiesNbt.setString("name", "textures");
                    propertiesNbt.setString("value", (String) getOptionValue(Food.Options.TEXTURE_VALUE));
                });
            } else {
                NBT.modify(itemStack, nbt -> {
                    ReadWriteNBT skullOwnerCompound = nbt.getOrCreateCompound("SkullOwner");
                    skullOwnerCompound.setUUID("Id", (UUID) getOptionValue(Food.Options.UUID));
                    skullOwnerCompound.getOrCreateCompound("Properties")
                            .getCompoundList("textures")
                            .addCompound()
                            .setString("Value", (String) getOptionValue(Food.Options.TEXTURE_VALUE));
                });
            }
        } else if (itemStack.getType() == Material.GLASS_BOTTLE) {
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            String colorCode = (String) getOptionValue(Food.Options.POTION_COLOR);
            Color color;
            java.awt.Color awtColor;
            try {
                awtColor = java.awt.Color.decode(colorCode);
                color = Color.fromBGR(awtColor.getBlue(), awtColor.getGreen(), awtColor.getRed());
            } catch (IllegalArgumentException | NullPointerException e) {
                color = Color.WHITE;
            }
            potionMeta.setColor(color);
            itemStack.setItemMeta(potionMeta);
        }

        return itemStack;
    }

}
