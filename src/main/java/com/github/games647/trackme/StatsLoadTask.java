package com.github.games647.trackme;

import java.util.UUID;

public class StatsLoadTask implements Runnable {

    private final TrackMe plugin;
    private final UUID loadUUID;
    private final String playerName;

    public StatsLoadTask(TrackMe plugin, UUID loadUUID, String playerName) {
        this.plugin = plugin;
        this.loadUUID = loadUUID;
        this.playerName = playerName;
    }

    @Override
    public void run() {
        PlayerStats loadedPlayer = plugin.getDatabaseManager().loadPlayer(loadUUID);
        if (loadedPlayer == null) {
            loadedPlayer = new PlayerStats(playerName, loadUUID);
        }

        plugin.getCache().put(loadUUID, loadedPlayer);
    }
}
