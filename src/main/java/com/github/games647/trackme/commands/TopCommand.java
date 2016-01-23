package com.github.games647.trackme.commands;

import com.github.games647.trackme.PlayerStats;
import com.github.games647.trackme.TrackMe;

import java.util.Arrays;
import java.util.List;
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

public class TopCommand implements CommandExecutor {

    private final TrackMe plugin;

    public TopCommand(TrackMe plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        int page = args.<Integer>getOne("page").get();

        plugin.getGame().getScheduler().createTaskBuilder()
                .async()
                .execute(() -> display(src, page, plugin.getDatabaseManager().getTopEntries(page)))
                .submit(plugin);

        src.sendMessage(Text.of(TextColors.YELLOW, "Query sent"));
        return CommandResult.success();
    }

    private void display(CommandSource src, int page, List<PlayerStats> topEntries) {
        if (src instanceof Player && plugin.getConfigManager().getConfiguration().isScoreboardDisplay()) {
            Player receiver = (Player) src;

            Objective objective = Objective.builder()
                    .displayName(Text.of(TextColors.GOLD, "Page ", page))
                    .name("topstats")
                    .criterion(Criteria.DUMMY)
                    .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                    .build();

            if (topEntries.isEmpty()) {
                objective.getOrCreateScore(Text.of(TextColors.DARK_RED, "No entries")).setScore(-1);
            } else {
                int rank = (page - 1) * 10 + 1;
                for (PlayerStats topEntry : topEntries) {
                    String playerName = topEntry.getPlayername();
                    int playerKills = topEntry.getPlayerKills();
                    objective.getOrCreateScore(Text.of(TextColors.DARK_AQUA, rank, ". ", playerName)).setScore(playerKills);
                    rank++;
                }
            }

            Scoreboard tempScoreboard = Scoreboard.builder()
                    .objectives(Arrays.asList(objective))
                    .build();
            tempScoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
            receiver.setScoreboard(tempScoreboard);

            plugin.getGame().getScheduler().createTaskBuilder().delay(5, TimeUnit.SECONDS)
                    .execute(() -> tempScoreboard.removeObjective(objective))
                    .submit(plugin);
        } else {
            src.sendMessage(Text.of(TextColors.GOLD, "Page " + page));

            if (topEntries.isEmpty()) {
                src.sendMessage(Text.of(TextColors.DARK_RED, "No entries"));
            } else {
                int rank = (page - 1) * 10 + 1;
                for (PlayerStats topEntry : topEntries) {
                    String playerName = topEntry.getPlayername();
                    int playerKills = topEntry.getPlayerKills();
                    src.sendMessage(Text.of(TextColors.DARK_AQUA, rank, ". ", playerName, " - ", playerKills));
                    rank++;
                }
            }
        }
    }
}
