package com.github.games647.trackme.commands;

import com.github.games647.trackme.PlayerStats;
import com.github.games647.trackme.TrackMe;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class StatsCommand implements CommandExecutor {

    private final TrackMe plugin;

    public StatsCommand(TrackMe plugin) {
        this.plugin = plugin;
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
        if (src instanceof Player && plugin.getConfigManager().getConfiguration().isScoreboardDisplay()) {
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
                    .objectives(Arrays.asList(objective))
                    .build();
            tempScoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
            receiver.setScoreboard(tempScoreboard);

            plugin.getGame().getScheduler().createTaskBuilder().delay(5, TimeUnit.SECONDS)
                    .execute(() -> tempScoreboard.removeObjective(objective))
                    .submit(plugin);
        } else {
            src.sendMessage(Text.of(TextColors.YELLOW, "Kills: " + stats.getPlayerKills()));
            src.sendMessage(Text.of(TextColors.YELLOW, "Mob kills: " + stats.getMobKills()));
            src.sendMessage(Text.of(TextColors.YELLOW, "Deaths: " + stats.getDeaths()));
        }
    }
}
