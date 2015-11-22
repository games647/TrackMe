package com.github.games647.trackme;

import java.util.UUID;

public class PlayerStats {

    private final String playername;
    private final UUID uuid;

    private int playerKills;
    private int mobKills;
    private int deaths;

    private long lastOnline;

    public PlayerStats(String playername, UUID uuid) {
        this.playername = playername;
        this.uuid = uuid;
    }

    public PlayerStats(String playername, UUID uuid, int playerKills, int mobKills, int deaths, long lastOnline) {
        this(playername, uuid);

        this.playerKills = playerKills;
        this.mobKills = mobKills;
        this.deaths = deaths;
        this.lastOnline = lastOnline;
    }

    public String getPlayername() {
        return playername;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(int playerKills) {
        this.playerKills = playerKills;
    }

    public int getMobKills() {
        return mobKills;
    }

    public void setMobKills(int mobKills) {
        this.mobKills = mobKills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public long getLastOnline() {
        return lastOnline;
    }
}
