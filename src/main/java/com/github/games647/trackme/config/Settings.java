package com.github.games647.trackme.config;

import com.github.games647.trackme.TrackMe;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Settings {

    private final ConfigurationLoader<CommentedConfigurationNode> configManager;
    private final Path defaultConfigFile;

    private final TrackMe plugin;

    private ObjectMapper<Config>.BoundInstance configMapper;
    private CommentedConfigurationNode rootNode;

    public Settings(ConfigurationLoader<CommentedConfigurationNode> configManager, Path defaultConfigFile
            , TrackMe plugin) {
        this.configManager = configManager;
        this.plugin = plugin;
        this.defaultConfigFile = defaultConfigFile;

        try {
            configMapper = ObjectMapper.forClass(Config.class).bindToNew();
        } catch (ObjectMappingException objMappingExc) {
            plugin.getLogger().error("Invalid plugin structure", objMappingExc);
        }
    }

    public void load() {
        if (!Files.exists(defaultConfigFile)) {
            try {
                Files.createFile(defaultConfigFile);
                rootNode = configManager.createEmptyNode(ConfigurationOptions.defaults());
            } catch (IOException ioExc) {
                plugin.getLogger().error("Error creating a new config file", ioExc);
                return;
            }
        }

        rootNode = configManager.createEmptyNode(ConfigurationOptions.defaults());
        if (configMapper != null) {
            try {
                rootNode = configManager.load();

                //load the config into the object
                configMapper.populate(rootNode);

                //add missing default values
                configMapper.serialize(rootNode);
                configManager.save(rootNode);
            } catch (ObjectMappingException objMappingExc) {
                plugin.getLogger().error("Error loading the configuration", objMappingExc);
            } catch (IOException ioExc) {
                plugin.getLogger().error("Error saving the default configuration", ioExc);
            }
        }
    }

    public void save() {
        if (configMapper != null && rootNode != null) {
            try {
                configMapper.serialize(rootNode);
                configManager.save(rootNode);
            } catch (ObjectMappingException objMappingExc) {
                plugin.getLogger().error("Error serialize the configuration", objMappingExc);
            } catch (IOException ioExc) {
                plugin.getLogger().error("Error saving the configuration", ioExc);
            }
        }
    }

    public Config getConfiguration() {
        if (configMapper == null) {
            return null;
        }

        return configMapper.getInstance();
    }

    public Path getConfigDir() {
        return defaultConfigFile.getParent();
    }
}
