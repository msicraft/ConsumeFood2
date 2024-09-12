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
                return List.of("reload", "customfood");
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("customfood")) {
                    return List.of("give");
                } else if (args[0].equalsIgnoreCase("hunger")) {
                    return List.of("get", "set", "add");
                }
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("customfood") && args[1].equalsIgnoreCase("give")) {
                    return plugin.getCustomFoodManager().getInternalNames();
                } else if (args[0].equalsIgnoreCase("hunger")) {
                    if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add")) {
                        return List.of("<amount>");
                    }
                }
            }
            if (args.length == 4) {
                if (args[0].equalsIgnoreCase("customfood") && args[1].equalsIgnoreCase("give")) {
                    return List.of("<amount>");
                }
            }
        }
        return List.of();
    }
}
