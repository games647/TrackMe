package com.github.games647.trackme;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

public class StatsCommand implements CommandExecutor {

    private final TrackMe plugin;

    public StatsCommand(TrackMe plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player targetPlayer = args.<Player>getOne("target").get();
        PlayerStats playerStats = plugin.getPlayerStats().get(targetPlayer.getUniqueId());
        if (playerStats != null) {
            src.sendMessage(Texts.of(TextColors.YELLOW, "Kills: " + playerStats.getPlayerKills()));
            src.sendMessage(Texts.of(TextColors.YELLOW, "Mob kills: " + playerStats.getMobKills()));
            src.sendMessage(Texts.of(TextColors.YELLOW, "Deaths: " + playerStats.getDeaths()));
        } else {
            src.sendMessage(Texts.of(TextColors.DARK_RED, "The requested player has no profile yet"));
        }

        return CommandResult.success();
    }
}
