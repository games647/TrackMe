package com.github.games647.trackme;



import com.github.games647.trackme.PlayerStats;
import com.github.games647.trackme.TrackMe;

public class StatsSaveTask implements Runnable {

    private final TrackMe plugin;
    private final PlayerStats toSave;

    public StatsSaveTask(TrackMe plugin, PlayerStats toSave) {
        this.plugin = plugin;
        this.toSave = toSave;
    }

    @Override
    public void run() {
        plugin.getDatabaseManager().savePlayer(toSave);
    }
}
