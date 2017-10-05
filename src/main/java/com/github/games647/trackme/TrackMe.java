package com.github.games647.trackme;

import com.github.games647.trackme.commands.StatsCommand;
import com.github.games647.trackme.commands.TopCommand;
import com.github.games647.trackme.config.Settings;
import com.github.games647.trackme.listener.ConnectionListener;
import com.github.games647.trackme.listener.PlayerListener;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION
        , url = PomData.URL, description = PomData.DESCRIPTION)
public class TrackMe {

    private final Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfigFile;

    private Settings configuration;
    private DatabaseManager databaseManager;

    private final Map<UUID, PlayerStats> playerStats = Maps.newConcurrentMap();

    @Inject
    public TrackMe(Logger logger) {
        this.logger = logger;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        configuration = new Settings(defaultConfigFile, this);
        configuration.load();

        databaseManager = new DatabaseManager(this);
        databaseManager.setupDatabase();
    }

    @Listener
    public void onInit(GameInitializationEvent initEvent) {
        //register events
        Sponge.getEventManager().registerListeners(this, new ConnectionListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerListener(this));

        //register commands
        CommandManager commandDispatcher = Sponge.getCommandManager();
        CommandSpec statsCommand = CommandSpec.builder()
                .executor(new StatsCommand(this))
                .permission(PomData.ARTIFACT_ID + ".command.stats")
                .arguments(GenericArguments
                        .onlyOne(GenericArguments
                                .playerOrSource(Text.of("target"))))
                .build();
        commandDispatcher.register(this, statsCommand, PomData.ARTIFACT_ID, "stats", "pvpstats");

        CommandSpec topCommand = CommandSpec.builder()
                .executor(new TopCommand(this))
                .permission(PomData.ARTIFACT_ID + ".command.top")
                .arguments(GenericArguments
                        .optional(GenericArguments
                                .integer(Text.of("page")), 1))
                .build();
        commandDispatcher.register(this, topCommand, "top", "topstats");
    }

    public Settings getConfigManager() {
        return configuration;
    }

    public Logger getLogger() {
        return logger;
    }

    public Map<UUID, PlayerStats> getCache() {
        return playerStats;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
