package me.msicraft.API.Food;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class Food {

    private final Material material;
    private int foodLevel = 0;
    private float saturation = 0;

    private final List<FoodPotionEffect> potionEffectList = new ArrayList<>();
    private final List<FoodCommandExecute> commandExecuteList = new ArrayList<>();

    public Food(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void addPotionEffect(PotionEffect potionEffect, double chance) {
        addPotionEffect(new FoodPotionEffect(potionEffect, chance));
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

    public void addCommandExecute(FoodCommandExecute foodCommandExecute) {
        commandExecuteList.add(foodCommandExecute);
    }

    public void addCommandExecute(String command, FoodCommandExecute.ExecuteType executeType) {
        addCommandExecute(new FoodCommandExecute(command, executeType));
    }

    public void removeAllCommandExecute() {
        commandExecuteList.clear();
    }

    public List<FoodCommandExecute> getCommandExecute() {
        return commandExecuteList;
    }

}
