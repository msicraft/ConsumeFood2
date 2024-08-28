package me.msicraft.consumefood2.Command;

import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {

    private final ConsumeFood2 plugin;

    public MainCommand(ConsumeFood2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("consumefood2")) {
            String var = args[0];
            switch (var) {
            }
        }
        return false;
    }

}