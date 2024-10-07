package me.msicraft.API;

import me.msicraft.API.CustomException.InvalidFormat;
import me.msicraft.API.CustomException.UnknownPotionEffectType;
import me.msicraft.API.Food.FoodCommand;
import me.msicraft.API.Food.FoodPotionEffect;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffectType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {

    private static Common instance;
    public static final String PREFIX = ChatColor.GREEN + "[ConsumeFood2] ";

    public static Common getInstance() {
        if (instance == null) {
            instance = new Common();
        }
        return instance;
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public String translateColorCodes(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String c = message.substring(matcher.start(), matcher.end());
            message = message.replace(c, net.md_5.bungee.api.ChatColor.of(c) + "");
            matcher = HEX_PATTERN.matcher(message);
        }
        return message;
    }

    public FoodPotionEffect formatToFoodPotionEffect(String format) throws UnknownPotionEffectType, InvalidFormat {
        try {
            String[] split = format.split(":");
            PotionEffectType potionEffectType = PotionEffectType.getByName(split[0].toUpperCase());
            if (potionEffectType != null) {
                int level = Integer.parseInt(split[1]);
                int duration;
                String durationS = split[2];
                if (durationS.contains("s")) {
                    durationS = durationS.replace("s", "");
                    durationS = durationS.replaceAll("[^0-9]", "");
                    duration = Integer.parseInt(durationS) * 20;
                } else {
                    duration = Integer.parseInt(split[2]);
                }
                String chanceS = split[3];
                double chance = 1.0;
                if (chanceS.contains("%")) {
                    chanceS = chanceS.replace("%", "");
                    chanceS = chanceS.replaceAll("[^0-9]", "");
                    int i = Integer.parseInt(chanceS);
                    if (i > 100) {
                        i = 100;
                    } else if (i < 0) {
                        i = 0;
                        chance = 0.0;
                    }
                    if (i != 100 && i != 0) {
                        chance = i / 100.0;
                    }
                } else {
                    chance = Double.parseDouble(chanceS);
                    if (chance > 1.0) {
                        chance = 1.0;
                    }
                    if (chance < 0) {
                        chance = 0.0;
                    }
                }
                return new FoodPotionEffect(potionEffectType, level, duration, chance);
            } else {
                throw new UnknownPotionEffectType(format);
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new InvalidFormat("Invalid PotionEffect Format", format, "<potionType>:<level>:<duration>:<chance>");
        }
    }

    public FoodCommand formatToFoodCommand(String format) throws InvalidFormat {
        try {
            String[] split = format.split(":");
            FoodCommand.ExecuteType executeType = FoodCommand.ExecuteType.valueOf(split[0].toUpperCase());
            String c = split[1];
            return new FoodCommand(c, executeType);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new InvalidFormat("Invalid FoodCommand Format", format, "<executeType>:<command>");
        }
    }

}
