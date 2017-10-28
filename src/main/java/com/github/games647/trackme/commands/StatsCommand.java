package com.github.games647.trackme.commands;

import com.github.games647.trackme.PlayerStats;
import com.github.games647.trackme.TrackMe;
import com.github.games647.trackme.config.Settings;
import com.google.inject.Inject;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class StatsCommand implements CommandExecutor {

    private final TrackMe plugin;
    private final Settings settings;

    @Inject
    StatsCommand(TrackMe plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player targetPlayer = args.<Player>getOne("target").get();
        PlayerStats playerStats = plugin.getCache().get(targetPlayer.getUniqueId());
        if (playerStats != null) {
            display(src, playerStats);
        } else {
            src.sendMessage(Text.of(TextColors.DARK_RED, "The requested player has no profile yet"));
        }

        return CommandResult.success();
    }

    private void display(CommandSource src, PlayerStats stats) {
        if (src instanceof Player && settings.getConfig().isScoreboardDisplay()) {
            Player receiver = (Player) src;

            Objective objective = Objective.builder()
                    .displayName(Text.of(TextColors.DARK_RED, stats.getPlayername()))
                    .name("playerstats")
                    .criterion(Criteria.DUMMY)
                    .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                    .build();

            objective.getOrCreateScore(Text.of(TextColors.YELLOW, "Kills")).setScore(stats.getPlayerKills());
            objective.getOrCreateScore(Text.of(TextColors.YELLOW, "MobKills")).setScore(stats.getMobKills());
            objective.getOrCreateScore(Text.of(TextColors.YELLOW, "Deaths")).setScore(stats.getDeaths());

            Scoreboard tempScoreboard = Scoreboard.builder()
                    .objectives(Collections.singletonList(objective))
                    .build();
            tempScoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
            receiver.setScoreboard(tempScoreboard);

            UUID uuid = receiver.getUniqueId();
            Task.builder().delay(5, TimeUnit.SECONDS)
                    .execute(() -> Sponge.getServer().getPlayer(uuid).ifPresent(player -> {
                        Scoreboard scoreboard = player.getScoreboard();
                        scoreboard.getObjective("playerstats").ifPresent(scoreboard::removeObjective);
                    })).submit(plugin);
        } else {
            src.sendMessage(Text.of(TextColors.YELLOW, "Kills: " + stats.getPlayerKills()));
            src.sendMessage(Text.of(TextColors.YELLOW, "Mob kills: " + stats.getMobKills()));
            src.sendMessage(Text.of(TextColors.YELLOW, "Deaths: " + stats.getDeaths()));
        }
    }
}
