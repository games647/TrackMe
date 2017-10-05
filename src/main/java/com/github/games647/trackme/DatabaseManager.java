package com.github.games647.trackme;

import com.github.games647.trackme.config.SQLConfiguration;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

public class DatabaseManager {

    private static final String STATS_TABLE = "playerstats";

    private final TrackMe plugin;
    private final String jdbcUrl;
    private SqlService sql;

    public DatabaseManager(TrackMe plugin) {
        this.plugin = plugin;

        SQLConfiguration sqlConfig = plugin.getConfigManager().getConfiguration().getSqlConfiguration();

        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(sqlConfig.getType().name().toLowerCase())
                .append("://");
        switch (sqlConfig.getType()) {
            case SQLITE:
                urlBuilder.append(sqlConfig.getPath()
                        .replace("%DIR%", plugin.getConfigManager().getConfigDir().normalize().toString()))
                        .append(File.separatorChar)
                        .append("database.db");
                break;
            case MYSQL:
                //jdbc:<engine>://[<username>[:<password>]@]<host>/<database> - copied from sponge doc
                urlBuilder.append(sqlConfig.getUsername());
                if (!sqlConfig.getPassword().isEmpty()) {
                    urlBuilder.append(':').append(sqlConfig.getPassword());
                }

                urlBuilder.append(sqlConfig.getUsername()).append('@')
                        .append(sqlConfig.getPath())
                        .append(':')
                        .append(sqlConfig.getPort())
                        .append('/')
                        .append(sqlConfig.getDatabase());
                break;
            case H2:
            default:
                urlBuilder.append(sqlConfig.getPath()
                        .replace("%DIR%", plugin.getConfigManager().getConfigDir().normalize().toString()))
                        .append(File.separatorChar)
                        .append("database");
                break;
        }

        this.jdbcUrl = urlBuilder.toString();
        this.sql = Sponge.getServiceManager().provideUnchecked(SqlService.class);
    }

    public Connection getConnection() throws SQLException {
        if (sql == null) {
            //lazy binding
            sql = Sponge.getServiceManager().provideUnchecked(SqlService.class);
        }

        return sql.getDataSource(jdbcUrl).getConnection();
    }

    public void setupDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + STATS_TABLE + " ( "
                    + "`UserID` INT UNSIGNED NOT NULL AUTO_INCREMENT , "
                    + "`UUID` BINARY(16) NOT NULL , "
                    + "`Username` VARCHAR(32) NOT NULL , "
                    + "`PlayerKills` INT NOT NULL DEFAULT 0, "
                    + "`MobKills` INT NOT NULL DEFAULT 0, "
                    + "`Deaths` INT NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (`UserID`) , UNIQUE (`UUID`) "
                    + ')');
            stmt.close();
        } catch (SQLException ex) {
            plugin.getLogger().error("Error creating database table", ex);
        }
    }

    public List<PlayerStats> getTopEntries(int page) {
        List<PlayerStats> result = Lists.newArrayList();

        int startIndex = (page - 1) * 10;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + STATS_TABLE
                     + " ORDER BY PlayerKills DESC"
                     + " LIMIT " + startIndex + ", 10")) {
            while (resultSet.next()) {
                result.add(new PlayerStats(resultSet));
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error loading stats", sqlEx);
        }

        return result;
    }

    public PlayerStats loadPlayer(UUID playerUUID) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + STATS_TABLE + " WHERE UUID=?")) {
            byte[] mostBytes = Longs.toByteArray(playerUUID.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(playerUUID.getLeastSignificantBits());

            stmt.setObject(1, Bytes.concat(mostBytes, leastBytes));

            try (ResultSet resultSet = stmt.executeQuery();) {
                if (resultSet.first()) {
                    return new PlayerStats(resultSet);
                }
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error loading stats", sqlEx);
        }

        return null;
    }

    public void savePlayer(PlayerStats playerStats) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE " + STATS_TABLE
                     + " SET Username=?, PlayerKills=?, MobKills=?, Deaths=?"
                     + " WHERE UUID=?")) {
            //username is now changeable by Mojang - so keep it up to date
            stmt.setString(1, playerStats.getPlayername());

            stmt.setInt(2, playerStats.getPlayerKills());
            stmt.setInt(3, playerStats.getMobKills());
            stmt.setInt(4, playerStats.getDeaths());

            UUID uuid = playerStats.getUuid();

            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            stmt.setObject(5, Bytes.concat(mostBytes, leastBytes));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                try (PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO " + STATS_TABLE
                        + " (UUID, Username, PlayerKills, MobKills, Deaths)"
                        + " VALUES(?, ?, ?, ?, ?)")) {
                    stmt2.setObject(1, Bytes.concat(mostBytes, leastBytes));

                    //username is now changeable by Mojang - so keep it up to date
                    stmt2.setString(2, playerStats.getPlayername());

                    stmt2.setInt(3, playerStats.getPlayerKills());
                    stmt2.setInt(4, playerStats.getMobKills());
                    stmt2.setInt(5, playerStats.getDeaths());
                    stmt2.execute();
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("Error saving player stats", ex);
        }
    }
}
