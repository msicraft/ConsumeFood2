package me.msicraft.API.Food;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Food {

    public enum Options {
        MATERIAL("Material", false, "Material", List.of("Food Material"), Material.APPLE, "Material", "Material"),
        TEXTURE_VALUE("String", true, "Texture Value", List.of("Texture value when Material is Player_Head"), "", "TextureValue", "TextureValue"),
        DISPLAYNAME("String", true, "Display Name", List.of("DisplayName of the item"),null, "DisplayName", "Name"),
        CUSTOM_MODEL_DATA("Integer", true, "Custom Model Data", List.of("CustomModelData"), -1, "CustomModelData", "Data"),
        LORE("None", true, "Lore", List.of("The lore of the item"),null, "Lore", "Lore"),
        POTION_EFFECT("None", false, "Potion Effect", List.of("Potion effects applied when consumed"), null, "PotionEffect", "PotionEffect"),
        COMMAND("None", false, "Command", List.of("Execute commands applied when consumed"), null, "Command", "Command"),
        FOOD_LEVEL("Integer", false, "Food Level", List.of("Amount of food level restored when consumed"),0, "FoodLevel", "FoodLevel"),
        SATURATION("Double", false, "Saturation", List.of("Amount of saturation restored when consumed"),0.0, "Saturation", "Saturation"),
        COOLDOWN("Double", false, "Personal CoolDown", List.of("The value used when the value of CooldownType is personal."), 0.0, "CoolDown", "Cooldown"),
        ENCHANT("None", true, "Enchant", List.of("List of enchantments to be applied to items"),null, "Enchant", "Enchant"),
        HIDE_ENCHANT("Boolean", true, "Hide Enchant", List.of("Whether or not the item is enchanted"),false, "HideEnchant", "HideEnchant"),
        DISABLE_CRAFTING("Boolean", true, "Disable Crafting", List.of("Prevent crafting of item"),false, "DisableCrafting", "DisableCrafting"),
        DISABLE_SMELTING("Boolean", true, "Disable Smelting", List.of("Prevent smelting of item"),false, "DisableSmelting", "DisableSmelting"),
        DISABLE_ANVIL("Boolean", true, "Disable Anvil", List.of("Prevent anvil of item"),false, "DisableAnvil", "DisableAnvil"),
        DISABLE_ENCHANT("Boolean", true, "Disable Enchant", List.of("Prevent enchant of item"),false, "DisableEnchant", "DisableEnchant"),
        SOUND("String", true, "Sound", List.of("Sound to be played when consumed"),null, "Sound", "Sound"),
        POTION_COLOR("String", true, "PotionColor", List.of("Value used when the material is a potion"),"#ffffff", "PotionColor", "PotionColor"),
        HIDE_POTION_EFFECT("Boolean", true, "Hide PotionEffect", List.of("Whether or not the item is potion effect"),false, "HidePotionEffect", "HidePotionEffect"),
        HIDE_ADDITIONAL_TOOLTIP("Boolean", true, "Hide Additional Tooltip", List.of("Hide potion effects, book and firework information, map tooltips, patterns of banners", "+ 1.20.5"),false, "HideAdditionalTooltip", null),
        UNSTACKABLE("Boolean", true, "UnStackable", List.of("Prevent stacking of items"),false, "UnStackable", "Unstackable"),
        INSTANT_EAT("Boolean", false, "Instant Eat", List.of("Right click to eat the item immediately"),false, "InstantEat", null),
        ALWAYS_EAT("Boolean", true, "Always Eat", List.of("Can eat anytime", "+ 1.20.5"),false, "AlwaysEat", null),
        EAT_SECONDS("Double", true, "Eat Seconds", List.of("Eat seconds", "+ 1.20.5"),-1.0, "EatSeconds", null),
        MAX_STACK_SIZE("Integer", true, "Max Stack Size", List.of("Max Stack Size","+ 1.20.5"),1, "MaxStackSize", null),
        UUID("None", true, "", List.of("UUID"),null, "UUID", "UUID");

        private final String valueType;
        private final boolean isCustomFoodOption;
        private final String displayName;
        private final List<String> description;
        private final Object baseValue;
        private final String path;
        private final String oldPath;

        Options(String valueType, boolean isCustomFoodOption, String displayName, List<String> description, Object baseValue, String path, String oldPath) {
            this.valueType = valueType;
            this.isCustomFoodOption = isCustomFoodOption;
            this.displayName = displayName;
            this.description = description;
            this.baseValue = baseValue;
            this.path = path;
            this.oldPath = oldPath;
        }

        public String getValueType() {
            return valueType;
        }

        public boolean isCustomFoodOption() {
            return isCustomFoodOption;
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<String> getDescription() {
            return description;
        }

        public Object getBaseValue() {
            return baseValue;
        }

        public String getPath() {
            return path;
        }

        public String getOldPath() {
            return oldPath;
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

    public void addPotionEffect(FoodPotionEffect foodPotionEffect) {
        potionEffectList.add(foodPotionEffect);
    }

    public List<FoodPotionEffect> getPotionEffects() {
        return potionEffectList;
    }

    public void clearPotionEffects() {
        potionEffectList.clear();
    }

    public void addCommand(FoodCommand foodCommand) {
        commandList.add(foodCommand);
    }

    public void addCommand(String command, FoodCommand.ExecuteType executeType) {
        addCommand(new FoodCommand(command, executeType));
    }

    public List<FoodCommand> getCommands() {
        return commandList;
    }

    public void clearCommands() {
        commandList.clear();
    }

    public void setOption(Options option, Object value) {
        if (option == Options.LORE || option == Options.ENCHANT || option == Options.POTION_EFFECT || option == Options.COMMAND) {
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
