package me.msicraft.API.Food;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FoodPotionEffect {

    private final PotionEffect potionEffect;
    private final double chance;

    private final int originalLevel;
    private final int originalDuration;

    public FoodPotionEffect(PotionEffectType potionEffectType, int level, int duration, double chance) {
        this.originalLevel = level;
        this.originalDuration = duration;
        level = level - 1;
        if (level < 0) {
            level = 0;
        }
        this.potionEffect = new PotionEffect(potionEffectType, duration, level);
        this.chance = chance;
    }

    /*
    private FoodPotionEffect(PotionEffect potionEffect, int chance) {
        double temp;
        this.potionEffect = potionEffect;
        temp = chance / 100.0;
        if (temp > 1) {
            temp = 1;
        }
        this.chance = temp;
    }

     */

    public double getChance() {
        return chance;
    }

    public PotionEffect getPotionEffect() {
        return potionEffect;
    }

    public int getOriginalLevel() {
        return originalLevel;
    }

    public int getOriginalDuration() {
        return originalDuration;
    }

    public String toFormat() {
        return potionEffect.getType().getName() + ":" + originalLevel + ":" + originalDuration + ":" + chance;
    }

}
