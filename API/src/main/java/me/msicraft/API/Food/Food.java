package me.msicraft.API.Food;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Food {

    public enum Options {
        MATERIAL(false, "Material", Material.APPLE, "Material"),
        TEXTURE_VALUE(true, "Texture Value", "", "TextureValue"),
        DISPLAYNAME(true, "Display Name", null, "DisplayName"),
        CUSTOM_MODEL_DATA(true, "Custom Model Data", -1, "CustomModelData"),
        LORE(true, "Lore", null, "Lore"),
        FOOD_LEVEL(false, "Food Level", 0, "FoodLevel"),
        SATURATION(false, "Saturation", 0.0F, "Saturation"),
        COOLDOWN(false, "Personal CoolDown", 0.0, "CoolDown"),
        ENCHANT(true, "Enchant", null, "Enchant"),
        HIDE_ENCHANT(true, "Hide Enchant", false, "HideEnchant"),
        DISABLE_CRAFTING(true, "Disable Crafting", false, "DisableCrafting"),
        DISABLE_SMELTING(true, "Disable Smelting", false, "DisableSmelting"),
        DISABLE_ANVIL(true, "Disable Anvil", false, "DisableAnvil"),
        DISABLE_ENCHANT(true, "Disable Enchant", false, "DisableEnchant"),
        SOUND(true, "Sound", null, "Sound"),
        POTION_COLOR(true, "PotionColor", "#ffffff", "PotionColor"),
        HIDE_POTION_EFFECT(true, "Hide PotionEffect", false, "HidePotionEffect"),
        UNSTACKABLE(true, "UnStackable", false, "UnStackable"),
        INSTANT_EAT(false, "Instant Eat", false, "InstantEat"),
        ALWAYS_EAT(true, "Always Eat", false, "AlwaysEat"),
        EAT_SECONDS(true, "Eat Seconds", -1F, "EatSeconds"),
        UUID(true, "", null, "UUID");

        private final boolean isCustomFoodOption;
        private final String displayName;
        private final Object baseValue;
        private final String path;

        Options(boolean isCustomFoodOption, String displayName, Object baseValue, String path) {
            this.isCustomFoodOption = isCustomFoodOption;
            this.displayName = displayName;
            this.baseValue = baseValue;
            this.path = path;
        }

        public boolean isCustomFoodOption() {
            return isCustomFoodOption;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Object getBaseValue() {
            return baseValue;
        }

        public String getPath() {
            return path;
        }
    }

    private final List<FoodPotionEffect> potionEffectList = new ArrayList<>();
    private final List<FoodCommand> commandList = new ArrayList<>();

    private final Map<Options, Object> optionMap = new HashMap<>();

    public Food(Material material) {
        setOption(Options.MATERIAL, material);
    }

    @NotNull
    public Material getMaterial() {
        return (Material) optionMap.getOrDefault(Options.MATERIAL, Material.APPLE);
    }

    public void addPotionEffect(PotionEffect potionEffect, double chance) {
        addPotionEffect(new FoodPotionEffect(potionEffect, chance));
    }

    public void addPotionEffect(PotionEffectType potionEffectType, int duration, int level, double chance) {
        addPotionEffect(new PotionEffect(potionEffectType, duration, level), chance);
    }

    public void addPotionEffect(PotionEffect potionEffect, int chance) {
        if (chance >= 0 && chance <= 1) {
            addPotionEffect(potionEffect, (double) chance / 100);
        }
    }

    public void addPotionEffect(FoodPotionEffect foodPotionEffect) {
        potionEffectList.add(foodPotionEffect);
    }

    public void removeAllPotionEffects() {
        potionEffectList.clear();
    }

    public List<FoodPotionEffect> getPotionEffects() {
        return potionEffectList;
    }

    public void addCommand(FoodCommand foodCommand) {
        commandList.add(foodCommand);
    }

    public void addCommand(String command, FoodCommand.ExecuteType executeType) {
        addCommand(new FoodCommand(command, executeType));
    }

    public void removeAllCommands() {
        commandList.clear();
    }

    public List<FoodCommand> getCommands() {
        return commandList;
    }

    public void setOption(Options option, Object value) {
        if (option == Options.LORE || option == Options.ENCHANT) {
            return;
        }
        optionMap.put(option, value);
    }

    public boolean hasOption(Options option) {
        return optionMap.containsKey(option);
    }

    public Object getOptionValue(Options option) {
        return optionMap.getOrDefault(option, option.getBaseValue());
    }

    public Set<Options> getOptions() {
        return optionMap.keySet();
    }

}
