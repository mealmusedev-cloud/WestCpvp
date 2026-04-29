package net.westcpvp.stats;

import org.bukkit.plugin.java.JavaPlugin;

public class WestCPvPStats extends JavaPlugin {

    private StatsManager statsManager;
    private LeaderboardAPI api;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int port  = getConfig().getInt("api-port", 8080);
        int limit = getConfig().getInt("leaderboard-size", 10);
        int saveInterval = getConfig().getInt("save-interval", 300);

        statsManager = new StatsManager(this);
        statsManager.load();

        api = new LeaderboardAPI(statsManager, port, limit, getLogger());
        api.start();

        getServer().getPluginManager().registerEvents(new PlayerListener(statsManager), this);

        StatsCommand cmd = new StatsCommand(statsManager, limit);
        getCommand("wcstats").setExecutor(cmd);
        getCommand("wcleaderboard").setExecutor(cmd);
        getCommand("wcreset").setExecutor(cmd);

        // Auto-save on a repeating task
        getServer().getScheduler().runTaskTimerAsynchronously(this,
            statsManager::save, saveInterval * 20L, saveInterval * 20L);

        getLogger().info("WestCPvPStats enabled.");
    }

    @Override
    public void onDisable() {
        statsManager.save();
        api.stop();
        getLogger().info("WestCPvPStats disabled.");
    }
}
