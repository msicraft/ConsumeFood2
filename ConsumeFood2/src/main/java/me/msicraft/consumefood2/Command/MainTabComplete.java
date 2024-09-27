package me.msicraft.consumefood2.Command;

import me.msicraft.consumefood.ConsumeFood;
import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainTabComplete implements TabCompleter {

    private final ConsumeFood2 plugin;

    public MainTabComplete(ConsumeFood2 plugin) {
        this.plugin = plugin;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("consumefood2")) {
            if (args.length == 1) {
                return List.of("reload", "customfood", "vanillafood", "foodlevel", "saturation", "migrate");
            }
            if (args.length == 2) {
                String var = args[0];
                if (var.equalsIgnoreCase("customfood")) {
                    return List.of("edit", "give", "create", "delete");
                } else if (var.equalsIgnoreCase("foodlevel") || var.equalsIgnoreCase("saturation")) {
                    return List.of("get", "set", "add");
                } else if (var.equalsIgnoreCase("vanillafood")) {
                    return List.of("edit", "give");
                } else if (var.equalsIgnoreCase("migrate")) {
                    return List.of("customfood", "vanillafood");
                }
            }
            if (args.length == 3) {
                String var = args[0];
                String var2 = args[1];
                if (var.equalsIgnoreCase("customfood")) {
                    if (var2.equalsIgnoreCase("give") || var2.equalsIgnoreCase("delete")) {
                        return plugin.getCustomFoodManager().getAllInternalNames();
                    } else if (var2.equalsIgnoreCase("create")) {
                        return List.of("<internalname>");
                    }
                } else if (var.equalsIgnoreCase("foodlevel") || var.equalsIgnoreCase("saturation")) {
                    if (var2.equalsIgnoreCase("set") || var2.equalsIgnoreCase("add")) {
                        return List.of("<amount>");
                    }
                } else if (var.equalsIgnoreCase("vanillafood")) {
                    if (var2.equalsIgnoreCase("give")) {
                        List<String> s = new ArrayList<>();
                        plugin.getVanillaFoodManager().getVanillaFoodMaterials().forEach(material -> {
                            s.add(material.name());
                        });
                        return s;
                    }
                } else if (var.equalsIgnoreCase("migrate")) {
                    if (Bukkit.getPluginManager().getPlugin("ConsumeFood") != null) {
                        if (var2.equalsIgnoreCase("customfood")) {
                            List<String> list = new ArrayList<>();
                            list.add("all-customfood");
                            ConfigurationSection section = ConsumeFood.customFoodConfig.getConfig().getConfigurationSection("CustomFood");
                            if (section != null) {
                                Set<String> keys = section.getKeys(false);
                                list.addAll(keys);
                                return list;
                            }
                        } else if (var2.equalsIgnoreCase("vanillafood")) {
                            List<String> list = new ArrayList<>();
                            list.add("all-vanillafood");
                            ConfigurationSection section = ConsumeFood.getPlugin().getConfig().getConfigurationSection("Food");
                            if (section != null) {
                                Set<String> keys = section.getKeys(false);
                                list.addAll(keys);
                                return list;
                            }
                        }
                    }
                }
            }
            if (args.length == 4) {
                String var = args[0];
                String var2 = args[1];
                if (var.equalsIgnoreCase("customfood") && var2.equalsIgnoreCase("give")) {
                    return List.of("<amount>");
                } else if (var.equalsIgnoreCase("vanillafood") && var2.equalsIgnoreCase("give")) {
                    return List.of("<amount>");
                }
            }
        }
        return null;
    }
}
