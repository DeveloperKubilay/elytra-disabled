package ua.krint.elytraDisabled;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ElytraPlaceholders extends PlaceholderExpansion {

    private final ElytraDisabled plugin;

    public ElytraPlaceholders(ElytraDisabled plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "elytradisabled";
    }

    @Override
    public String getAuthor() {
        return "Krint";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (!(offlinePlayer instanceof Player)) return "";
        Player player = (Player) offlinePlayer;

        switch (params.toLowerCase()) {
            case "world_disabled":
                return String.valueOf(plugin.isWorldDisabled(player.getWorld()));
            case "bypass":
                return String.valueOf(plugin.hasBypass(player));
            case "blocked":
                return String.valueOf(plugin.isWorldDisabled(player.getWorld()) && !plugin.hasBypass(player));
            default:
                return null;
        }
    }
}
