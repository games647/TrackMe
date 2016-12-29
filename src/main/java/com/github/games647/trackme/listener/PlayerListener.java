package com.github.games647.trackme.listener;

import com.github.games647.trackme.PlayerStats;
import com.github.games647.trackme.TrackMe;


import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

public class PlayerListener {

    private final TrackMe plugin;

    public PlayerListener(TrackMe plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onEntityDeath(DestructEntityEvent.Death destructEntityEvent, @First EntityDamageSource damageSource) {
        Living targetEntity = destructEntityEvent.getTargetEntity();
        if (targetEntity instanceof Player) {
            Player targetPlayer = (Player) targetEntity;
            increaseDeaths(targetPlayer);
        }

        Entity killerEntity = damageSource.getSource();
        if (killerEntity instanceof Player) {
            Player killerPlayer = (Player) killerEntity;
            if (targetEntity instanceof Player) {
                increasePlayerKills(killerPlayer);
            } else {
                increaseMobKills(killerPlayer);
            }
        }
    }

    private void increaseMobKills(Player sourcePlayer) {
        PlayerStats playerStats = plugin.getCache().get(sourcePlayer.getUniqueId());
        if (playerStats != null) {
            int lastMobKills = playerStats.getDeaths();
            playerStats.setMobKills(lastMobKills + 1);
        }
    }

    private void increasePlayerKills(Player sourcePlayer) {
        PlayerStats playerStats = plugin.getCache().get(sourcePlayer.getUniqueId());
        if (playerStats != null) {
            int lastPlayerKills = playerStats.getPlayerKills();
            playerStats.setPlayerKills(lastPlayerKills + 1);
        }
    }

    private void increaseDeaths(Player targetPlayer) {
        PlayerStats playerStats = plugin.getCache().get(targetPlayer.getUniqueId());
        if (playerStats != null) {
            int lastDeaths = playerStats.getDeaths();
            playerStats.setDeaths(lastDeaths + 1);
        }
    }
}
