package com.github.games647.trackme.config;

import ninja.leaping.configurate.objectmapping.Setting;

public class Config {

    @Setting(comment = "Database configuration")
    private SQLConfiguration sqlConfiguration = new SQLConfiguration();

    public SQLConfiguration getSqlConfiguration() {
        return sqlConfiguration;
    }
}
