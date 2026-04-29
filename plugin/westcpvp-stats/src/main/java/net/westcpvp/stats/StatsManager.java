package net.westcpvp.stats;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager {

    private final JavaPlugin plugin;
    private final Map<UUID, PlayerStats> stats = new ConcurrentHashMap<>();
    private final File dataFile;

    public StatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "stats.json");
    }

    public PlayerStats get(UUID uuid, String name) {
        return stats.computeIfAbsent(uuid, k -> new PlayerStats(name));
    }

    public void addKill(UUID uuid, String name) {
        get(uuid, name).addKill();
    }

    public void addDeath(UUID uuid, String name) {
        PlayerStats s = get(uuid, name);
        s.setName(name);
        s.addDeath();
    }

    public List<PlayerStats> getTopByKills(int limit) {
        List<PlayerStats> list = new ArrayList<>(stats.values());
        list.sort(Comparator.comparingInt(PlayerStats::getKills).reversed());
        return list.subList(0, Math.min(limit, list.size()));
    }

    public void load() {
        if (!dataFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            parseJson(sb.toString());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load stats: " + e.getMessage());
        }
    }

    public void save() {
        plugin.getDataFolder().mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile))) {
            writer.print(buildJson());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save stats: " + e.getMessage());
        }
    }

    private String buildJson() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<UUID, PlayerStats> entry : stats.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            PlayerStats s = entry.getValue();
            sb.append("\"").append(entry.getKey()).append("\":{")
              .append("\"name\":\"").append(escapeJson(s.getName())).append("\",")
              .append("\"kills\":").append(s.getKills()).append(",")
              .append("\"deaths\":").append(s.getDeaths()).append(",")
              .append("\"currentStreak\":").append(s.getCurrentStreak()).append(",")
              .append("\"bestStreak\":").append(s.getBestStreak())
              .append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    private void parseJson(String json) {
        // Simple parser for our known format
        json = json.trim();
        if (json.equals("{}") || json.isEmpty()) return;
        // Strip outer braces
        json = json.substring(1, json.length() - 1).trim();
        // Split by top-level UUID keys
        int i = 0;
        while (i < json.length()) {
            if (json.charAt(i) == '"') {
                int uuidEnd = json.indexOf('"', i + 1);
                String uuidStr = json.substring(i + 1, uuidEnd);
                int objStart = json.indexOf('{', uuidEnd);
                int objEnd = findMatchingBrace(json, objStart);
                String obj = json.substring(objStart + 1, objEnd);
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    PlayerStats s = parsePlayerStats(obj);
                    stats.put(uuid, s);
                } catch (Exception ignored) {}
                i = objEnd + 1;
                // skip comma
                while (i < json.length() && (json.charAt(i) == ',' || json.charAt(i) == ' ')) i++;
            } else {
                i++;
            }
        }
    }

    private PlayerStats parsePlayerStats(String obj) {
        PlayerStats s = new PlayerStats(extractString(obj, "name"));
        s.setKills(extractInt(obj, "kills"));
        s.setDeaths(extractInt(obj, "deaths"));
        s.setCurrentStreak(extractInt(obj, "currentStreak"));
        s.setBestStreak(extractInt(obj, "bestStreak"));
        return s;
    }

    private String extractString(String obj, String key) {
        String search = "\"" + key + "\":\"";
        int start = obj.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = obj.indexOf('"', start);
        return end == -1 ? "" : obj.substring(start, end);
    }

    private int extractInt(String obj, String key) {
        String search = "\"" + key + "\":";
        int start = obj.indexOf(search);
        if (start == -1) return 0;
        start += search.length();
        int end = start;
        while (end < obj.length() && Character.isDigit(obj.charAt(end))) end++;
        try { return Integer.parseInt(obj.substring(start, end)); } catch (Exception e) { return 0; }
    }

    private int findMatchingBrace(String s, int start) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '{') depth++;
            else if (s.charAt(i) == '}') { depth--; if (depth == 0) return i; }
        }
        return s.length() - 1;
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
