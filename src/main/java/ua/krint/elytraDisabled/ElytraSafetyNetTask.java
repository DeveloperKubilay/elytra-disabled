package ua.krint.elytraDisabled;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class ElytraSafetyNetTask {

    private final ElytraDisabled plugin;
    private BukkitTask task;

    public ElytraSafetyNetTask(ElytraDisabled plugin) {
        this.plugin = plugin;
    }

    public void start(long intervalTicks) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::check, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    private void check() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        boolean forceUnequip = plugin.isForceUnequipOnEnter();
        boolean stopGlide = plugin.isStopExistingGlide();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.hasBypass(p)) continue;
            if (!plugin.isWorldDisabled(p.getWorld())) continue;

            if (forceUnequip) {
                ItemStack chestplate = p.getInventory().getChestplate();
                if (chestplate != null && chestplate.getType() == Material.ELYTRA) {
                    plugin.removeElytra(p, null);
                    plugin.sendBlockedWarning(p, "equip_blocked");
                }
            }

            if (stopGlide && p.isGliding()) {
                p.setGliding(false);
                if (!forceUnequip) {
                    plugin.sendBlockedWarning(p, "glide_blocked");
                }
            }
        }
    }
}
