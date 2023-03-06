package io.noks.smallpractice.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

import com.zaxxer.hikari.HikariDataSource;

import io.noks.smallpractice.enums.Ladders;
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
	private final String SAVE = "UPDATE players SET =? WHERE uuid=?";
	private String INSERT;
	private final String SELECT = "SELECT nodebuff,archer,axe,soup,early-hg,gapple,boxing,combo,sumo,noenchant FROM players WHERE uuid=?";
	
	public DBUtils(String address, String name, String user, String password) {
		this.address = address;
		this.name = name;
		this.username = user;
		this.password = password;
		//this.connectDatabase();
	}

	public void connectDatabase() {
		try {
			this.hikari = new HikariDataSource();
			this.hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
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
			createTable();
		} catch (Exception exception) {
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
		this.INSERT = "INSERT INTO players VALUES(?, " + questionMarks.toString() + "?) ON DUPLICATE KEY UPDATE uuid=?";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(this.INSERT);

			statement.setString(1, uuid.toString());
			int i;
			for (i = 1; i < Ladders.values().length + 1; i++) {
				statement.setInt(i, 0);
			}
			statement.setString(i + 1, uuid.toString());
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
				new PlayerManager(uuid, new EloManager(elos));
			}
			statement.close();
			result.close();
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

	private void createTable() {
		if (!isConnected()) {
			return;
		}
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = (PreparedStatement) connection.createStatement();
			final StringJoiner ladder = new StringJoiner(", ");
			for (Ladders ladders : Ladders.values()) {
				ladder.add(ladders.getName().toLowerCase() + " int(11)");
			}
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS players(uuid varchar(36), " + ladder.toString() + "PRIMARY KEY(`uuid`), UNIQUE(`uuid`));");
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

	public void savePlayer(PlayerManager pm) {
		if (!isConnected()) {
			pm.remove();
			return;
		}
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(this.SAVE);
			
			int i = 1;
			for (Ladders ladders : Ladders.values()) {
				statement.setInt(i, pm.getEloManager().getFrom(ladders));
				i++;
			}
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

	public HikariDataSource getHikari() {
		return this.hikari;
	}

	public boolean isConnected() {
		return this.connected;
	}
}
