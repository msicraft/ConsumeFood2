package me.msicraft.consumefood2.API;

import me.msicraft.consumefood2.ConsumeFood2;
import org.bukkit.Bukkit;

import java.util.function.Consumer;

public class BukkitChecker {

    private final ConsumeFood2 plugin;
    private final int resourceId;

    public BukkitChecker(ConsumeFood2 plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public String getBukkitVersion() {
        String v = null;
        try {
            v = Bukkit.getBukkitVersion().split("-")[0];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        return v;
    }

    public void getPluginUpdateCheck(Consumer<String> consumer) {
        /*
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                plugin.getLogger().info("Unable to check for updates: " + exception.getMessage());
            }
        });

         */
    }

}
