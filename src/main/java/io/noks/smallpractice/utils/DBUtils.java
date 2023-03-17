package io.noks.smallpractice.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import com.zaxxer.hikari.HikariDataSource;

import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.objects.PlayerSettings;
import io.noks.smallpractice.objects.managers.EloManager;
import io.noks.smallpractice.objects.managers.PlayerManager;
import net.minecraft.util.com.google.common.collect.Lists;

public class DBUtils {
	private boolean connected = false;
	private final String address;
	private final String name;
	private final String username;
	private final String password;

	private HikariDataSource hikari;
	private final String SELECT = "SELECT * FROM players WHERE uuid=?";
	
	// TODO: 2v2 = uuid1 uuid2 allLadders ez
	// TODO: loadDuoElo and ensure to check the order 
	
	public DBUtils(String address, String name, String user, String password) {
		this.address = address;
		this.name = name;
		this.username = user;
		this.password = password;
		this.connectDatabase();
	}

	public void connectDatabase() {
		if (this.address.length() == 0 || this.name.length() == 0 || this.username.length() == 0 || this.password.length() == 0) {
			return;
		}
		try {
			this.hikari = new HikariDataSource();
			this.hikari.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource"); //com.mysql.jdbc.jdbc2.optional.MysqlDataSource
			this.hikari.addDataSourceProperty("serverName", this.address);
			this.hikari.addDataSourceProperty("port", "3306");
			this.hikari.addDataSourceProperty("databaseName", this.name);
			this.hikari.addDataSourceProperty("user", this.username);
			this.hikari.addDataSourceProperty("password", this.password);
			// KEEP THE CONNECTION OPEN WITH HIKARI - start
			this.hikari.addDataSourceProperty("autoReconnect", Boolean.valueOf(true));
			this.hikari.addDataSourceProperty("cachePrepStmts", Boolean.valueOf(true));
			this.hikari.addDataSourceProperty("prepStmtCacheSize", Integer.valueOf(250));
			this.hikari.addDataSourceProperty("prepStmtCacheSqlLimit", Integer.valueOf(2048));
			this.hikari.addDataSourceProperty("useServerPrepStmts", Boolean.valueOf(true));
			this.hikari.addDataSourceProperty("cacheResultSetMetadata", Boolean.valueOf(true));
			this.hikari.setMaximumPoolSize(20);
			this.hikari.setConnectionTimeout(30000L);
			// KEEP THE CONNECTION OPEN WITH HIKARI - end
			this.connected = true;
		} catch (Exception exception) {
		}
		this.createTable();
	}
	
	private void createTable() {
		if (!isConnected()) {
			return;
		}
		Connection connection = null;
		final StringJoiner ladder = new StringJoiner(", ");
		for (Ladders ladders : Ladders.values()) {
			ladder.add(ladders.getName().toLowerCase() + " int(4)");
		}
		try {
			connection = this.hikari.getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid varchar(36), " + ladder.toString() + ", pingdiff int(3), PRIMARY KEY(`uuid`), UNIQUE(`uuid`));");
			//statement.executeUpdate("CREATE TABLE IF NOT EXISTS elo (uuid varchar(36), " + ladder.toString() + ", PRIMARY KEY(`uuid`), UNIQUE(`uuid`));");
			//statement.executeUpdate("CREATE TABLE IF NOT EXISTS settings (uuid varchar(36), pingdiff int(3), tpm TINYINT(1), invite TINYINT(1), request TINYINT(1), PRIMARY KEY(`uuid`), UNIQUE(`uuid`));");
			//statement.executeUpdate("CREATE TABLE IF NOT EXISTS duoelo (uuid1 varchar(36), uuid2 varchar(36), " + ladder.toString() + ", PRIMARY KEY(uuid1, uuid2), UNIQUE KEY(uuid1, uuid2));");
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public void loadPlayer(UUID uuid) {
		if (!isConnected()) {
			new PlayerManager(uuid).heal(false);
			return;
		}
		final StringJoiner questionMarks = new StringJoiner(", ");
		for (int i = 0; i < Ladders.values().length; i++) {
			questionMarks.add("?");
		}
		final String INSERT = "INSERT INTO players VALUES(?, " + questionMarks.toString() + ", ?) ON DUPLICATE KEY UPDATE uuid=?";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(INSERT);

			int i = 1;
			statement.setString(i, uuid.toString());
			while (i != Ladders.values().length + 1) {
				i++;
				statement.setInt(i, 1200);
			}
			statement.setInt(i + 1, 300);
			statement.setString(i + 2, uuid.toString());
			statement.executeUpdate();
			statement.close();

			statement = connection.prepareStatement(this.SELECT);
			statement.setString(1, uuid.toString());
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				final List<Integer> elos = Lists.newArrayList();
				for (Ladders ladders : Ladders.values()) {
					elos.add(result.getInt(ladders.getName().toLowerCase()));
				}
				new PlayerManager(uuid, new EloManager(elos), new PlayerSettings(result.getInt("pingdiff")));
			}
			result.close();
			statement.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public void savePlayer(PlayerManager pm) {
		if (!isConnected()) {
			pm.remove();
			return;
		}
		final StringJoiner ladder = new StringJoiner(", ");
		for (Ladders ladders : Ladders.values()) {
			ladder.add(ladders.getName().toLowerCase() + "=?");
		}
		final String SAVE = "UPDATE players SET " + ladder.toString() + ", pingdiff=? WHERE uuid=?";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(SAVE);
			
			int i = 1;
			for (Ladders ladders : Ladders.values()) {
				statement.setInt(i, pm.getEloManager().getFrom(ladders));
				i++;
			}
			statement.setInt(i, pm.getSettings().getQueuePingDiff());
			statement.setString(i + 1, pm.getPlayerUUID().toString());
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			pm.remove();
		}
	}
	
	public void savePlayerSingleElo(PlayerManager pm, Ladders ladder) {
		if (!isConnected()) {
			return;
		}
		final String SAVE = "UPDATE players SET " + ladder.getName().toLowerCase() + "=? WHERE uuid=?";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(SAVE);
			
			statement.setInt(1, pm.getEloManager().getFrom(ladder));
			statement.setString(2, pm.getPlayerUUID().toString());
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public Map<UUID, Integer> getTopEloLadder(Ladders ladder) {
		if (!isConnected()) {
			return null;
		}
		final Map<UUID, Integer> map = new LinkedHashMap<UUID, Integer>();
		final String selectLine = "SELECT uuid," + ladder.getName().toLowerCase() + " FROM players ORDER BY " + ladder.getName().toLowerCase() + " DESC LIMIT 10";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(selectLine);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				final UUID uuid = UUID.fromString(result.getString("uuid"));
				final int elo = result.getInt(ladder.getName().toLowerCase());
				map.put(uuid, elo);
			}
			result.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
		return map;
	}
	
	public Map<UUID, Integer> getGlobalTopElo() {
		return null;
	}
	
	public HikariDataSource getHikari() {
		return this.hikari;
	}

	public boolean isConnected() {
		return this.connected;
	}
}
