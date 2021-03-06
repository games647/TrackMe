package com.github.games647.trackme;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerStats {

    private final String playername;
    private final UUID uuid;

    private int playerKills;
    private int mobKills;
    private int deaths;

    public PlayerStats(String playername, UUID uuid) {
        this.playername = playername;
        this.uuid = uuid;
    }

    public PlayerStats(ResultSet resultSet) throws SQLException {
        ByteBuffer uuidBytes = ByteBuffer.wrap(resultSet.getBytes(2));

        this.uuid = new UUID(uuidBytes.getLong(), uuidBytes.getLong());
        this.playername = resultSet.getString(3);
        this.playerKills = resultSet.getInt(4);
        this.mobKills = resultSet.getInt(5);
        this.deaths = resultSet.getInt(6);
    }

    public PlayerStats(String playername, UUID uuid, int playerKills, int mobKills, int deaths) {
        this(playername, uuid);

        this.playerKills = playerKills;
        this.mobKills = mobKills;
        this.deaths = deaths;
    }

    public String getPlayername() {
        return playername;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(int playerKills) {
        this.playerKills = playerKills;
    }

    public int getMobKills() {
        return mobKills;
    }

    public void setMobKills(int mobKills) {
        this.mobKills = mobKills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
}
