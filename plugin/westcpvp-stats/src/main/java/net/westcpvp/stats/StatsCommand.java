package net.westcpvp.stats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class StatsCommand implements CommandExecutor {

    private final StatsManager statsManager;
    private final int limit;

    public StatsCommand(StatsManager statsManager, int limit) {
        this.statsManager = statsManager;
        this.limit = limit;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String name = command.getName().toLowerCase();

        if (name.equals("wcstats")) {
            String targetName = args.length > 0 ? args[0] : (sender instanceof Player p ? p.getName() : null);
            if (targetName == null) {
                sender.sendMessage("§cUsage: /wcstats <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(targetName);
            UUID uuid = target != null ? target.getUniqueId() : null;
            if (uuid == null) {
                // Try to find offline player by name from stored stats
                sender.sendMessage("§cPlayer not found or has no stats yet.");
                return true;
            }
            PlayerStats s = statsManager.get(uuid, targetName);
            sender.sendMessage("§b§lStats for " + s.getName());
            sender.sendMessage("§7Kills: §f" + s.getKills());
            sender.sendMessage("§7Deaths: §f" + s.getDeaths());
            sender.sendMessage("§7K/D: §f" + String.format("%.2f", s.getKdr()));
            sender.sendMessage("§7Best Streak: §f" + s.getBestStreak());
            sender.sendMessage("§7Current Streak: §f" + s.getCurrentStreak());
            return true;
        }

        if (name.equals("wcleaderboard")) {
            List<PlayerStats> top = statsManager.getTopByKills(limit);
            sender.sendMessage("§b§l--- WestCPvP Leaderboard (Top " + top.size() + ") ---");
            for (int i = 0; i < top.size(); i++) {
                PlayerStats s = top.get(i);
                sender.sendMessage(String.format("§7#%d §f%s §7- Kills: §a%d §7K/D: §e%.2f §7Best: §c%d",
                    i + 1, s.getName(), s.getKills(), s.getKdr(), s.getBestStreak()));
            }
            return true;
        }

        if (name.equals("wcreset")) {
            if (!sender.hasPermission("westcpvp.stats.reset")) {
                sender.sendMessage("§cYou don't have permission.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /wcreset <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer must be online to reset.");
                return true;
            }
            PlayerStats fresh = statsManager.get(target.getUniqueId(), target.getName());
            fresh.setKills(0);
            fresh.setDeaths(0);
            fresh.setCurrentStreak(0);
            fresh.setBestStreak(0);
            sender.sendMessage("§aReset stats for §f" + target.getName());
            return true;
        }

        return true;
    }
}
