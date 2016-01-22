package com.github.games647.trackme.config;

import ninja.leaping.configurate.objectmapping.Setting;

public class Config {

    @Setting(comment = "Database configuration")
    private SQLConfiguration sqlConfiguration = new SQLConfiguration();

    @Setting(comment = "Should the top and stats command be displayed on the scoreboard")
    private boolean scoreboardDisplay = true;

    public SQLConfiguration getSqlConfiguration() {
        return sqlConfiguration;
    }

    public boolean isScoreboardDisplay() {
        return scoreboardDisplay;
    }
}
