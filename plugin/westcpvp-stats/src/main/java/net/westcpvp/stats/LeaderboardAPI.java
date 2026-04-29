package net.westcpvp.stats;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Logger;

public class LeaderboardAPI {

    private final StatsManager statsManager;
    private final int port;
    private final int limit;
    private final Logger logger;
    private HttpServer server;

    public LeaderboardAPI(StatsManager statsManager, int port, int limit, Logger logger) {
        this.statsManager = statsManager;
        this.port = port;
        this.limit = limit;
        this.logger = logger;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/leaderboard", exchange -> {
                String method = exchange.getRequestMethod();
                // Preflight CORS
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                exchange.getResponseHeaders().add("Cache-Control", "no-cache");

                if (method.equalsIgnoreCase("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                byte[] response = buildLeaderboardJson().getBytes("UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });

            server.createContext("/health", exchange -> {
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                byte[] response = "ok".getBytes();
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });

            server.setExecutor(null);
            server.start();
            logger.info("[WestCPvPStats] Leaderboard API started on port " + port);
        } catch (IOException e) {
            logger.severe("[WestCPvPStats] Failed to start API on port " + port + ": " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            logger.info("[WestCPvPStats] Leaderboard API stopped.");
        }
    }

    private String buildLeaderboardJson() {
        List<PlayerStats> top = statsManager.getTopByKills(limit);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < top.size(); i++) {
            if (i > 0) sb.append(",");
            PlayerStats s = top.get(i);
            sb.append("{")
              .append("\"rank\":").append(i + 1).append(",")
              .append("\"name\":\"").append(escape(s.getName())).append("\",")
              .append("\"kills\":").append(s.getKills()).append(",")
              .append("\"deaths\":").append(s.getDeaths()).append(",")
              .append("\"kdr\":").append(String.format("%.2f", s.getKdr())).append(",")
              .append("\"bestStreak\":").append(s.getBestStreak())
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
