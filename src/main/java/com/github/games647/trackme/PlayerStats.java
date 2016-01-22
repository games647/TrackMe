package com.github.games647.trackme;

import com.google.common.primitives.Longs;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

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
        byte[] uuidBytes = resultSet.getBytes(2);

        byte[] mostBits = ArrayUtils.subarray(uuidBytes, 0, 8);
        byte[] leastBits = ArrayUtils.subarray(uuidBytes, 8, 16);

        long mostByte = Longs.fromByteArray(mostBits);
        long leastByte = Longs.fromByteArray(leastBits);

        this.uuid = new UUID(mostByte, leastByte);
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

    private long parseLeastSignificant(byte[] byteArray) {
        long value = 0;
        for (int i = 0; i < byteArray.length; i++) {
            value += ((long) byteArray[i] & 0xffL) << (8 * i);
        }

        return value;
    }

    private long parseMostSignificant(byte[] byteArray) {
        long value = 0;
        for (byte aByte : byteArray) {
            value = (value << 8) + (aByte & 0xff);
        }

        return value;
    }
}
