package com.github.games647.trackme.commands;

import com.github.games647.trackme.PlayerStats;
import com.github.games647.trackme.TrackMe;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;

public class TopCommand implements CommandExecutor {

    private final TrackMe plugin;

    public TopCommand(TrackMe plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        int page = args.<Integer>getOne("page").get();

        Consumer<Collection<PlayerStats>> callback = map -> displayConsole(page, map);
        if (src instanceof Player) {
            UUID uuid = ((Player) src).getUniqueId();
            callback = map -> Sponge.getServer().getPlayer(uuid).ifPresent(player -> display(player, page, map));
        }

        SpongeExecutorService asyncExecutor = Sponge.getScheduler().createAsyncExecutor(plugin);
        SpongeExecutorService syncExecutor = Sponge.getScheduler().createAsyncExecutor(plugin);
        CompletableFuture.supplyAsync(() -> plugin.getDatabaseManager().getTopEntries(page), asyncExecutor)
                .thenAcceptAsync(callback, syncExecutor);

        src.sendMessage(Text.of(TextColors.YELLOW, "Query sent"));
        return CommandResult.success();
    }

    private void displayConsole(int page, Collection<PlayerStats> topEntries) {
        ConsoleSource src = Sponge.getServer().getConsole();
        displayText(src, page, topEntries);
    }

    private void display(Player receiver, int page, Collection<PlayerStats> topEntries) {
        if (plugin.getConfigManager().getConfiguration().isScoreboardDisplay()) {
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
            displayText(receiver, page, topEntries);
        }
    }

    private void displayText(MessageReceiver receiver, int page, Collection<PlayerStats> topEntries) {
        receiver.sendMessage(Text.of(TextColors.GOLD, "Page " + page));

        if (topEntries.isEmpty()) {
            receiver.sendMessage(Text.of(TextColors.DARK_RED, "No entries"));
        } else {
            int rank = (page - 1) * 10 + 1;
            for (PlayerStats topEntry : topEntries) {
                String playerName = topEntry.getPlayername();
                int playerKills = topEntry.getPlayerKills();
                receiver.sendMessage(Text.of(TextColors.DARK_AQUA, rank, ". ", playerName, " - ", playerKills));
                rank++;
            }
        }
    }
}
