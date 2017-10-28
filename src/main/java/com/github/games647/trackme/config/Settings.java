package com.github.games647.trackme.config;

import com.google.inject.Inject;

import java.io.IOException;
import java.nio.file.Path;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;

public class Settings {

    private final Logger logger;
    private final Path configDir;
    private final ConfigurationLoader<CommentedConfigurationNode> configManager;

    private ObjectMapper<Config>.BoundInstance configMapper;

    @Inject
    Settings(Logger logger, @DefaultConfig(sharedRoot = false) Path configDir,
             @DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> configManager) {
        this.logger = logger;
        this.configDir = configDir;
        this.configManager = configManager;

        try {
            configMapper = ObjectMapper.forClass(Config.class).bindToNew();
        } catch (ObjectMappingException objMappingExc) {
            logger.error("Invalid plugin structure", objMappingExc);
        }
    }

    public void load() {
        if (configMapper != null) {
            CommentedConfigurationNode rootNode;
            try {
                rootNode = configManager.load();

                //load the config into the object
                configMapper.populate(rootNode);

                //add missing default values
                configMapper.serialize(rootNode);
                configManager.save(rootNode);
            } catch (ObjectMappingException objMappingExc) {
                logger.error("Error loading the configuration", objMappingExc);
            } catch (IOException ioExc) {
                logger.error("Error saving the default configuration", ioExc);
            }
        }
    }

    public Config getConfig() {
        if (configMapper == null) {
            return null;
        }

        return configMapper.getInstance();
    }

    public Path getConfigDir() {
        return configDir;
    }
}
