package me.msicraft.API.Food;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Food {

    public enum Options {
        MATERIAL(false, "Material", Material.APPLE),
        TEXTURE_VALUE(true, "Texture Value", ""),
        DISPLAYNAME(true, "DisplayName", null),
        CUSTOM_MODEL_DATA(true, "CustomModelData", -1),
        LORE(true, "Lore", Collections.EMPTY_LIST),
        FOOD_LEVEL(false, "Food Level", 0),
        SATURATION(false, "Saturation", 0.0F),
        COOLDOWN(false, "Personal CoolDown", 0.0),
        ENCHANT(true, "Enchant", Collections.EMPTY_LIST),
        HIDE_ENCHANT(true, "Hide Enchant", false),
        DisableCrafting(true, "Disable Crafting", false),
        DisableSmelting(true, "Disable Smelting", false),
        DisableAnvil(true, "Disable Anvil", false),
        DisableEnchant(true, "Disable Enchant", false),
        SOUND(true, "Sound", null),
        POTION_COLOR(true, "PotionColor", ""),
        HIDE_POTION_EFFECT(true, "Hide PotionEffect", false),
        UNSTACKABLE(true, "UnStackable", false),
        INSTANT_EAT(false, "Instant Eat", false),
        ALWAYS_EAT(true, "Always Eat", false),
        EAT_SECONDS(true, "Eat Seconds", -1F);

        private final boolean isCustomFoodOption;
        private final String displayName;
        private final Object baseValue;

        Options(boolean isCustomFoodOption, String displayName, Object baseValue) {
            this.isCustomFoodOption = isCustomFoodOption;
            this.displayName = displayName;
            this.baseValue = baseValue;
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

    }

    private final List<FoodPotionEffect> potionEffectList = new ArrayList<>();
    private final List<FoodCommand> commandList = new ArrayList<>();

    private final Map<Options, Object> optionMap = new HashMap<>();

    public Food(Material material) {
        addOption(Options.MATERIAL, material);
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

    public void addOption(Options option, Object value) {
        optionMap.put(option, value);
    }

    public Object getOptionValue(Options option) {
        return optionMap.getOrDefault(option, option.getBaseValue());
    }

}
