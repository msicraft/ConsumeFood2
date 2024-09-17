package me.msicraft.consumefood2.Command;

import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
                return List.of("reload", "customfood", "foodlevel", "saturation");
            }
            if (args.length == 2) {
                String var = args[0];
                if (var.equalsIgnoreCase("customfood")) {
                    return List.of("edit", "give", "create", "delete");
                } else if (var.equalsIgnoreCase("foodlevel") || var.equalsIgnoreCase("saturation")) {
                    return List.of("get", "set", "add");
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
                }
            }
            if (args.length == 4) {
                String var = args[0];
                String var2 = args[1];
                if (var.equalsIgnoreCase("customfood") && var2.equalsIgnoreCase("give")) {
                    return List.of("<amount>");
                }
            }
        }
        return null;
    }
}
