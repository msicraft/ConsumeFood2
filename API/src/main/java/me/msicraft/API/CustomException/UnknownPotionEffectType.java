package me.msicraft.API.CustomException;

public class UnknownPotionEffectType extends RuntimeException {

    public UnknownPotionEffectType(String format) {
        super("Unknown PotionEffect Type -> format: " + format);
    }

}
