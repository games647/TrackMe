package com.github.games647.trackme;

import java.util.Optional;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerListener {

    private final TrackMe plugin;

    public PlayerListener(TrackMe plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onEntityDeath(DestructEntityEvent.Death destructEntityEvent) {
        Living targetEntity = destructEntityEvent.getTargetEntity();
        if (targetEntity instanceof Player) {
            Player targetPlayer = (Player) targetEntity;
            increaseDeaths(targetPlayer);
        }

        Optional<EntityDamageSource> optionalKiller = destructEntityEvent.getCause().first(EntityDamageSource.class);
        if (optionalKiller.isPresent()) {
            Entity killerEntity = optionalKiller.get().getSource();
            if (killerEntity instanceof Player) {
                Player killerPlayer = (Player) killerEntity;
                if (targetEntity instanceof Player) {
                    increasePlayerKills(killerPlayer);
                } else {
                    increaseMobKills(killerPlayer);
                }
            }
        }
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect disconnectEvent) {
        Player player = disconnectEvent.getTargetEntity();
        //prevent memory leaks
        plugin.getPlayerStats().remove(player.getUniqueId());
    }

    private void increaseMobKills(Player sourcePlayer) {
        PlayerStats playerStats = plugin.getPlayerStats().get(sourcePlayer.getUniqueId());
        if (playerStats == null) {
            playerStats = createNewProfile(sourcePlayer);
        }

        int lastMobKills = playerStats.getDeaths();
        playerStats.setMobKills(lastMobKills + 1);
    }

    private void increasePlayerKills(Player sourcePlayer) {
        PlayerStats playerStats = plugin.getPlayerStats().get(sourcePlayer.getUniqueId());
        if (playerStats == null) {
            playerStats = createNewProfile(sourcePlayer);
        }

        int lastPlayerKills = playerStats.getPlayerKills();
        playerStats.setPlayerKills(lastPlayerKills + 1);
    }

    private void increaseDeaths(Player targetPlayer) {
        PlayerStats playerStats = plugin.getPlayerStats().get(targetPlayer.getUniqueId());
        if (playerStats == null) {
            playerStats = createNewProfile(targetPlayer);
        }

        int lastDeaths = playerStats.getDeaths();
        playerStats.setDeaths(lastDeaths + 1);
    }

    private PlayerStats createNewProfile(Player sourcePlayer) {
        PlayerStats playerStats = new PlayerStats(sourcePlayer.getName(), sourcePlayer.getUniqueId());
        plugin.getPlayerStats().put(sourcePlayer.getUniqueId(), playerStats);
        return playerStats;
    }
}
