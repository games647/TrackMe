package com.github.games647.trackme.listener;

import com.github.games647.trackme.PlayerStats;
import com.github.games647.trackme.StatsLoadTask;
import com.github.games647.trackme.TrackMe;

import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class ConnectionListener {

    private final TrackMe plugin;

    public ConnectionListener(TrackMe plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join joinEvent) {
        Player player = joinEvent.getTargetEntity();
        UUID uniqueId = player.getUniqueId();

        plugin.getGame().getScheduler().createTaskBuilder()
                .async()
                .execute(new StatsLoadTask(plugin, uniqueId, player.getName()))
                .submit(plugin);

    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect quitEvent) {
        Player player = quitEvent.getTargetEntity();
        UUID uniqueId = player.getUniqueId();

        PlayerStats removedStats = plugin.getCache().remove(uniqueId);
        if (removedStats != null) {
            plugin.getGame().getScheduler().createTaskBuilder()
                    .async()
                    .execute(() -> plugin.getDatabaseManager().savePlayer(removedStats))
                    .submit(plugin);
        }
    }
}
