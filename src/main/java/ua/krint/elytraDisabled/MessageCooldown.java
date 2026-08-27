package ua.krint.elytraDisabled;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MessageCooldown {

    private final Map<UUID, Long> lastSent = new ConcurrentHashMap<>();

    public boolean allow(Player p, long cooldownMillis) {
        UUID uuid = p.getUniqueId();
        long now = System.currentTimeMillis();

        Long last = lastSent.get(uuid);
        if (last != null && (now - last) < cooldownMillis) {
            return false;
        }

        lastSent.put(uuid, now);
        return true;
    }

    public void cleanUp(long cooldownMillis) {
        long now = System.currentTimeMillis();
        lastSent.entrySet().removeIf(entry -> (now - entry.getValue()) > cooldownMillis * 2);
    }
}
