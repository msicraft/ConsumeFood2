package me.msicraft.API.Food;

import org.bukkit.potion.PotionEffect;

public class FoodPotionEffect {

    private final PotionEffect potionEffect;
    private final double chance;

    public FoodPotionEffect(PotionEffect potionEffect, double chance) {
        this.potionEffect = potionEffect;
        this.chance = chance;
    }

    public FoodPotionEffect(PotionEffect potionEffect, int chance) {
        double temp;
        this.potionEffect = potionEffect;
        temp = chance / 100.0;
        if (temp > 1) {
            temp = 1;
        }
        this.chance = temp;
    }

    public double getChance() {
        return chance;
    }

    public PotionEffect getPotionEffect() {
        return potionEffect;
    }

    public String toFormat() {
        return potionEffect.getType().getName() + ":" + potionEffect.getAmplifier() + ":" + potionEffect.getDuration() + ":" + chance;
    }

}
