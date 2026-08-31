package ua.krint.elytraDisabled;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class PaperArmorChangeListener implements Listener {

    private final ElytraDisabled plugin;

    public PaperArmorChangeListener(ElytraDisabled plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorChange(PlayerArmorChangeEvent e) {
        if (e.getSlotType() != PlayerArmorChangeEvent.SlotType.CHEST) return;

        ItemStack newItem = e.getNewItem();
        if (newItem == null || newItem.getType() != Material.ELYTRA) return;

        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.isPreventEquip()) return;

        plugin.removeElytra(p, null);
        plugin.sendBlockedWarning(p, "equip_blocked");
    }
}
