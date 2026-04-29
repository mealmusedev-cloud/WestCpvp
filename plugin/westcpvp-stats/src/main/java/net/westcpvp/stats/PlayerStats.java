package net.westcpvp.stats;

public class PlayerStats {

    private String name;
    private int kills;
    private int deaths;
    private int currentStreak;
    private int bestStreak;

    public PlayerStats(String name) {
        this.name = name;
    }

    public void addKill() {
        kills++;
        currentStreak++;
        if (currentStreak > bestStreak) bestStreak = currentStreak;
    }

    public void addDeath() {
        deaths++;
        currentStreak = 0;
    }

    public double getKdr() {
        return deaths == 0 ? kills : Math.round((double) kills / deaths * 100.0) / 100.0;
    }

    public String getName()        { return name; }
    public void   setName(String n){ this.name = n; }
    public int    getKills()       { return kills; }
    public void   setKills(int k)  { this.kills = k; }
    public int    getDeaths()      { return deaths; }
    public void   setDeaths(int d) { this.deaths = d; }
    public int    getCurrentStreak()     { return currentStreak; }
    public void   setCurrentStreak(int s){ this.currentStreak = s; }
    public int    getBestStreak()        { return bestStreak; }
    public void   setBestStreak(int s)   { this.bestStreak = s; }
}
