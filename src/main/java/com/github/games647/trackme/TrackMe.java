package com.github.games647.trackme;

import com.github.games647.trackme.config.Settings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandSpec;

@Plugin(id = "trackme", name = "TrackMe", version = "0.1")
public class TrackMe {

    private final PluginContainer pluginContainer;
    private final Logger logger;
    private final Game game;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File defaultConfigFile;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private Settings configuration;

    private final Map<UUID, PlayerStats> playerStats = Maps.newHashMap();

    @Inject
    public TrackMe(Logger logger, PluginContainer pluginContainer, Game game) {
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.game = game;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        logger.info("Loading {} v{}", pluginContainer.getName(), pluginContainer.getVersion());

        configuration = new Settings(configManager, defaultConfigFile, this);
        configuration.load();
    }

    @Listener
    public void onInit(GameInitializationEvent initEvent) {
        //register events
        initEvent.getGame().getEventManager().registerListeners(this, new PlayerListener(this));

        //register commands
        CommandService commandDispatcher = initEvent.getGame().getCommandDispatcher();
        CommandSpec statsCommand = CommandSpec.builder()
                .executor(new StatsCommand(this))
                .arguments(GenericArguments
                        .onlyOne(GenericArguments
                                .playerOrSource(Texts.of("target"), game)))
                .build();
        commandDispatcher.register(this, statsCommand, pluginContainer.getId(), "stats");
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

    public Map<UUID, PlayerStats> getPlayerStats() {
        return playerStats;
    }
}
