package us.noks.smallpractice.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class DBUtils {
	private static DBUtils instance = new DBUtils();
	public static DBUtils getInstance() {
		return instance;
	}

	private boolean connected = true;
	private String address = Main.getInstance().getConfig().getString("database.address");
	private String name = Main.getInstance().getConfig().getString("database.name");
	private String username = Main.getInstance().getConfig().getString("database.username");
	private String password = Main.getInstance().getConfig().getString("database.password");

	private HikariDataSource hikari;
	private final String SAVE = "UPDATE players SET kills=? WHERE uuid=?";
	private final String INSERT = "INSERT INTO players VALUES(?, ?) ON DUPLICATE KEY UPDATE uuid=?";
	private final String SELECT = "SELECT kills FROM players WHERE uuid=?";

	public void connectDatabase() {
		try {
			this.hikari = new HikariDataSource();
			this.hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
			this.hikari.addDataSourceProperty("serverName", this.address);
			this.hikari.addDataSourceProperty("port", "3306");
			this.hikari.addDataSourceProperty("databaseName", this.name);
			this.hikari.addDataSourceProperty("user", this.username);
			this.hikari.addDataSourceProperty("password", this.password);
			this.hikari.addDataSourceProperty("autoReconnect", Boolean.valueOf(true));
			this.hikari.addDataSourceProperty("cachePrepStmts", Boolean.valueOf(true));
			this.hikari.addDataSourceProperty("prepStmtCacheSize", Integer.valueOf(250));
			this.hikari.addDataSourceProperty("prepStmtCacheSqlLimit", Integer.valueOf(2048));
			this.hikari.addDataSourceProperty("useServerPrepStmts", Boolean.valueOf(true));
			this.hikari.addDataSourceProperty("cacheResultSetMetadata", Boolean.valueOf(true));
			this.hikari.setMaximumPoolSize(20);
			this.hikari.setConnectionTimeout(30000L);
			createTable();
		} catch (Exception exception) {
		}
	}

	public void loadPlayer(PlayerManager pm) {
		if (!isConnected()) {
			return;
		}
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(this.INSERT);

			statement.setString(1, pm.getPlayerUUID().toString());
			statement.setInt(2, 0);
			statement.setString(3, pm.getPlayerUUID().toString());
			statement.executeUpdate();
			statement.close();

			statement = connection.prepareStatement(this.SELECT);
			statement.setString(1, pm.getPlayerUUID().toString());
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				//DO SOMETHING
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
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS players(uuid varchar(36), kills int(11), PRIMARY KEY(`uuid`), UNIQUE(`uuid`));");
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
			return;
		}
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(this.SAVE);

			statement.setInt(1, 1);
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

	public HikariDataSource getHikari() {
		return this.hikari;
	}

	public boolean isConnected() {
		return this.connected;
	}
}
