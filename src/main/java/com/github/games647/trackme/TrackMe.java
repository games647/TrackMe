package com.github.games647.trackme;

import com.github.games647.trackme.commands.StatsCommand;
import com.github.games647.trackme.commands.TopCommand;
import com.github.games647.trackme.listener.ConnectionListener;
import com.github.games647.trackme.listener.PlayerListener;
import com.github.games647.trackme.config.Settings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import me.flibio.updatifier.Updatifier;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

@Updatifier(repoOwner = "games647", repoName = "TrackMe", version = "0.3.1")
@Plugin(id = "trackme", name = "TrackMe", version = "0.3.1"
        , url = "https://github.com/games647/TrackMe"
        , description = "A simple Sponge plugin which tracks pvp/pve kills and deaths.")
public class TrackMe {

    private final PluginContainer pluginContainer;
    private final Logger logger;
    private final Game game;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private File defaultConfigFile;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private Settings configuration;
    private DatabaseManager databaseManager;

    private final Map<UUID, PlayerStats> playerStats = Maps.newConcurrentMap();

    @Inject
    public TrackMe(Logger logger, PluginContainer pluginContainer, Game game) {
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.game = game;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        configuration = new Settings(configManager, defaultConfigFile, this);
        configuration.load();

        databaseManager = new DatabaseManager(this);
        databaseManager.setupDatabase();
    }

    @Listener
    public void onInit(GameInitializationEvent initEvent) {
        //register events
        game.getEventManager().registerListeners(this, new ConnectionListener(this));
        game.getEventManager().registerListeners(this, new PlayerListener(this));

        //register commands
        CommandManager commandDispatcher = game.getCommandManager();
        CommandSpec statsCommand = CommandSpec.builder()
                .executor(new StatsCommand(this))
                .permission(pluginContainer.getUnqualifiedId() + ".command.stats")
                .arguments(GenericArguments
                        .onlyOne(GenericArguments
                                .playerOrSource(Text.of("target"))))
                .build();
        commandDispatcher.register(this, statsCommand, pluginContainer.getUnqualifiedId(), "stats", "pvpstats");

        CommandSpec topCommand = CommandSpec.builder()
                .executor(new TopCommand(this))
                .permission(pluginContainer.getUnqualifiedId() + ".command.top")
                .arguments(GenericArguments
                        .optional(GenericArguments
                                .integer(Text.of("page")), 1))
                .build();
        commandDispatcher.register(this, topCommand, "top", "topstats");
    }

    public Settings getConfigManager() {
        return configuration;
    }

    public PluginContainer getContainer() {
        return pluginContainer;
    }

    public Logger getLogger() {
        return logger;
    }

    public Game getGame() {
        return game;
    }

    public Map<UUID, PlayerStats> getCache() {
        return playerStats;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
