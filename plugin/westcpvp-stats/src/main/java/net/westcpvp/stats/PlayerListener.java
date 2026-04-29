package net.westcpvp.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener {

    private final StatsManager statsManager;

    public PlayerListener(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        statsManager.addDeath(victim.getUniqueId(), victim.getName());

        if (killer != null && !killer.equals(victim)) {
            statsManager.addKill(killer.getUniqueId(), killer.getName());
        }
    }
}
