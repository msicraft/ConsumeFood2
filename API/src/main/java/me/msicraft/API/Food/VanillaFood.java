package me.msicraft.API.Food;

import org.bukkit.Material;

public class VanillaFood extends Food {

    public enum Type {
        APPLE(false, false, 4, 2.4), BAKED_POTATO(false, false, 5, 6),
        BEEF(false, false, 3, 1.8), BEETROOT(false, false, 1, 1.2),
        BEETROOT_SOUP(false, true, 6, 7.2), BREAD(false, false, 5, 6),
        CARROT(false, false, 3, 3.6), CHICKEN(false, false, 2, 2.1),
        COD(false, false, 2, 0.4), COOKED_BEEF(false, false, 8, 12.8),
        COOKED_CHICKEN(false, false, 6, 7.2), COOKED_COD(false, false, 5, 6.0),
        COOKED_MUTTON(false, false, 6, 9.6), COOKED_PORKCHOP(false, false, 8, 12.8),
        COOKED_RABBIT(false, false, 5, 6), COOKED_SALMON(false, false, 6, 9.6),
        COOKIE(false, false, 2, 0.5), DRIED_KELP(false, false, 1, 0.6),
        ENCHANTED_GOLDEN_APPLE(false, false, 4, 9.6), GLOW_BERRIES(false, false, 2, 0.4),
        GOLDEN_APPLE(false, false, 4, 9.6), GOLDEN_CARROT(false, false, 6, 14.4),
        HONEY_BOTTLE(true, false, 6, 1.2), MELON_SLICE(false, false, 2, 1.2),
        MUSHROOM_STEW(false, true, 6, 7.2), MUTTON(false, false, 2, 1.2),
        POISONOUS_POTATO(false, false, 2, 1.2), PORKCHOP(false, false, 3,0.6),
        POTATO(false, false, 1, 0.6), PUFFERFISH(false, false, 1, 0.2),
        PUMPKIN_PIE(false, false, 8, 4.8), RABBIT(false, false, 2, 1.2),
        RABBIT_STEW(false, true, 10, 12), ROTTEN_FLESH(false, false, 4, 0.8),
        SALMON(false, false, 2, 0.2), SPIDER_EYE(false, false, 2, 3.2),
        SWEET_BERRIES(false, false, 1, 0.0), TROPICAL_FISH(false, false, 1, 0.2);

        private final boolean isBottle;
        private final boolean isBowl;
        private final int foodLevel;
        private final double saturation;

        Type(boolean isBottle, boolean isBowl, int foodLevel, double saturation) {
            this.isBottle = isBottle;
            this.isBowl = isBowl;
            this.foodLevel = foodLevel;
            this.saturation = saturation;
        }

        public boolean isBottle() {
            return isBottle;
        }

        public boolean isBowl() {
            return isBowl;
        }

        public int getBaseFoodLevel() {
            return foodLevel;
        }

        public double getBaseSaturation() {
            return saturation;
        }

    }

    public VanillaFood(Material material) {
        super(material);
    }

}
