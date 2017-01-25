package com.github.games647.trackme;

import com.github.games647.trackme.config.SQLConfiguration;
import com.github.games647.trackme.config.SQLType;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import org.spongepowered.api.service.sql.SqlService;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private static final String STATS_TABLE = "playerstats";

    private final TrackMe plugin;

    private final String username;
    private final String password;

    private final String jdbcUrl;
    private SqlService sql;

    public DatabaseManager(TrackMe plugin) {
        this.plugin = plugin;

        SQLConfiguration sqlConfig = plugin.getConfigManager().getConfiguration().getSqlConfiguration();
        if (sqlConfig.getType() == SQLType.MYSQL) {
            this.username = sqlConfig.getUsername();
            this.password = sqlConfig.getPassword();
        } else {
            //flat file drivers throw exception if you try to connect with a account
            this.username = "";
            this.password = "";
        }

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
                urlBuilder.append(username);
                if (!password.isEmpty()) {
                    urlBuilder.append(':').append(password);
                }

                urlBuilder.append(password).append('@')
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
        this.sql = plugin.getGame().getServiceManager().provideUnchecked(SqlService.class);
    }

    public Connection getConnection() throws SQLException {
        if (sql == null) {
            //lazy binding
            sql = plugin.getGame().getServiceManager().provideUnchecked(SqlService.class);
        }

        return sql.getDataSource(jdbcUrl).getConnection();
    }

    public void setupDatabase() {
        Connection conn = null;
        try {
            conn = getConnection();

            boolean tableExists = false;
            try {
                //check if the table already exists
                Statement statement = conn.createStatement();
                statement.execute("SELECT 1 FROM " + STATS_TABLE);
                statement.close();

                tableExists = true;
            } catch (SQLException sqlEx) {
                plugin.getLogger().debug("Table doesn't exist", sqlEx);
            }

            if (!tableExists) {
                Statement statement = conn.createStatement();
                statement.execute("CREATE TABLE " + STATS_TABLE + " ( "
                        + "`UserID` INT UNSIGNED NOT NULL AUTO_INCREMENT , "
                        + "`UUID` BINARY(16) NOT NULL , "
                        + "`Username` VARCHAR(32) NOT NULL , "
                        + "`PlayerKills` INT NOT NULL DEFAULT 0, "
                        + "`MobKills` INT NOT NULL DEFAULT 0, "
                        + "`Deaths` INT NOT NULL DEFAULT 0, "
                        + "PRIMARY KEY (`UserID`) , UNIQUE (`UUID`) "
                        + ")");
                statement.close();
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("Error creating database table", ex);
        } finally {
            closeQuietly(conn);
        }
    }

    public List<PlayerStats> getTopEntries(int page) {
        List<PlayerStats> result = Lists.newArrayList();

        int startIndex = (page - 1) * 10;

        Connection conn = null;
        try {
            conn = getConnection();

            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + STATS_TABLE
                    + " ORDER BY PlayerKills DESC"
                    + " LIMIT " + startIndex + ", 10");

            while (resultSet.next()) {
                result.add(new PlayerStats(resultSet));
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error loading stats", sqlEx);
        } finally {
            closeQuietly(conn);
        }

        return result;
    }

    public PlayerStats loadPlayer(String playerName) {
        Connection conn = null;
        try {
            conn = getConnection();

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM " + STATS_TABLE + " WHERE Username=?");
            statement.setString(1, playerName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.first()) {
                return new PlayerStats(resultSet);
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error loading stats", sqlEx);
        } finally {
            closeQuietly(conn);
        }

        return null;
    }

    public PlayerStats loadPlayer(UUID playerUUID) {
        Connection conn = null;
        try {
            conn = getConnection();

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM " + STATS_TABLE + " WHERE UUID=?");

            byte[] mostBytes = Longs.toByteArray(playerUUID.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(playerUUID.getLeastSignificantBits());

            statement.setObject(1, Bytes.concat(mostBytes, leastBytes));

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.first()) {
                return new PlayerStats(resultSet);
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().error("Error loading stats", sqlEx);
        } finally {
            closeQuietly(conn);
        }

        return null;
    }

    public void savePlayer(PlayerStats playerStats) {
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("UPDATE " + STATS_TABLE
                    + " SET Username=?, PlayerKills=?, MobKills=?, Deaths=?"
                    + " WHERE UUID=?");

            //username is now changeable by Mojang - so keep it up to date
            statement.setString(1, playerStats.getPlayername());

            statement.setInt(2, playerStats.getPlayerKills());
            statement.setInt(3, playerStats.getMobKills());
            statement.setInt(4, playerStats.getDeaths());

            UUID uuid = playerStats.getUuid();

            byte[] mostBytes = Longs.toByteArray(uuid.getMostSignificantBits());
            byte[] leastBytes = Longs.toByteArray(uuid.getLeastSignificantBits());

            statement.setObject(5, Bytes.concat(mostBytes, leastBytes));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                statement = conn.prepareStatement("INSERT INTO " + STATS_TABLE
                        + " (UUID, Username, PlayerKills, MobKills, Deaths)"
                        + " VALUES(?, ?, ?, ?, ?)");

                statement.setObject(1, Bytes.concat(mostBytes, leastBytes));

                //username is now changeable by Mojang - so keep it up to date
                statement.setString(2, playerStats.getPlayername());

                statement.setInt(3, playerStats.getPlayerKills());
                statement.setInt(4, playerStats.getMobKills());
                statement.setInt(5, playerStats.getDeaths());
                statement.execute();
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("Error saving player stats", ex);
        } finally {
            closeQuietly(conn);
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                //this closes automatically the statement and resultset
                conn.close();
            } catch (SQLException ex) {
                //ingore
            }
        }
    }
}
