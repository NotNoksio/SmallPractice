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
import io.noks.smallpractice.party.Party;
import net.minecraft.util.com.google.common.collect.Lists;

public class DBUtils {
	private boolean connected = false;
	private final String address;
	private final String name;
	private final String username;
	private final String password;

	private HikariDataSource hikari;
	
	public DBUtils(String address, String name, String user, String password) {
		this.address = address;
		this.name = name;
		this.username = user;
		this.password = password;
		this.connectDatabase();
	}
	
	// TODO: ensure that the TABLE is created before going in it

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
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS elo (uuid varchar(36), " + ladder.toString() + ", global int(4), unrankedwin int(2), PRIMARY KEY(`uuid`), UNIQUE(`uuid`));");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS settings (uuid varchar(36), pingdiff int(3), tpm TINYINT(1), invite TINYINT(1), request TINYINT(1), PRIMARY KEY(`uuid`), UNIQUE(`uuid`));");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS duoelo (uuid1 varchar(36), uuid2 varchar(36), " + ladder.toString() + ", global int(4), PRIMARY KEY(uuid1, uuid2), CONSTRAINT unique_uuid_pair UNIQUE KEY(uuid1, uuid2), KEY(uuid1, uuid2));");
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
		final String INSERT_ELO = "INSERT INTO elo VALUES(?, " + questionMarks.toString() + ", ?, ?) ON DUPLICATE KEY UPDATE uuid=?";
		final String SELECT_ELO = "SELECT * FROM elo WHERE uuid=?";
		final String INSERT_SETTINGS = "INSERT INTO settings VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid=?";
		final String SELECT_SETTINGS = "SELECT * FROM settings WHERE uuid=?";
		Connection connection = null;
		EloManager elo = null;
		PlayerSettings settings = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(INSERT_ELO);
			
			int i = 1;
			statement.setString(i, uuid.toString());
			while (i != Ladders.values().length + 1) {
				i++;
				statement.setInt(i, 1200);
			}
			statement.setInt(i + 1, 1200);
			statement.setInt(i + 2, 0);
			statement.setString(i + 3, uuid.toString());
			statement.executeUpdate();
			statement.close();
			
			statement = connection.prepareStatement(INSERT_SETTINGS);
			statement.setString(1, uuid.toString());
			statement.setInt(2, 300);
			statement.setBoolean(3, true);
			statement.setBoolean(4, true);
			statement.setBoolean(5, true);
			statement.setString(6, uuid.toString());
			statement.executeUpdate();
			statement.close();

			statement = connection.prepareStatement(SELECT_ELO);
			statement.setString(1, uuid.toString());
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				final List<Integer> elos = Lists.newArrayList();
				for (Ladders ladders : Ladders.values()) {
					elos.add(result.getInt(ladders.getName().toLowerCase()));
				}
				elo = new EloManager(elos, result.getInt("unrankedwin"));
			}
			result.close();
			
			statement = connection.prepareStatement(SELECT_SETTINGS);
			statement.setString(1, uuid.toString());
			result = statement.executeQuery();
			if (result.next()) {
				settings = new PlayerSettings(result.getInt("pingdiff"), result.getBoolean("tpm"), result.getBoolean("invite"), result.getBoolean("request"));
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
			new PlayerManager(uuid, elo, settings);
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
		final String SAVE_ELO = "UPDATE elo SET " + ladder.toString() + ", global=?, unrankedwin=? WHERE uuid=?";
		final String SAVE_SETTINGS = "UPDATE settings SET pingdiff=?, tpm=?, invite=?, request=? WHERE uuid=?";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(SAVE_ELO);
			
			int i = 1;
			for (Ladders ladders : Ladders.values()) {
				statement.setInt(i, pm.getEloManager().getFrom(ladders));
				i++;
			}
			statement.setInt(i, pm.getEloManager().getGlobal());
			statement.setInt(i + 1, pm.getEloManager().getWinnedUnranked());
			statement.setString(i + 2, pm.getPlayerUUID().toString());
			statement.execute();
			statement.close();
			
			statement = connection.prepareStatement(SAVE_SETTINGS);
			statement.setInt(1, pm.getSettings().getQueuePingDiff());
			statement.setBoolean(2, pm.getSettings().isPrivateMessageToggled());
			statement.setBoolean(3, pm.getSettings().isPartyInviteToggled());
			statement.setBoolean(4, pm.getSettings().isDuelRequestToggled());
			statement.setString(5, pm.getPlayerUUID().toString());
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
		final String SAVE = "UPDATE elo SET " + ladder.getName().toLowerCase() + "=?, global=? WHERE uuid=?";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(SAVE);
			
			statement.setInt(1, pm.getEloManager().getFrom(ladder));
			statement.setInt(2, pm.getEloManager().getGlobal());
			statement.setString(3, pm.getPlayerUUID().toString());
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
	
	public void saveDuoElo(Party party, Ladders ladder) {
		if (!isConnected()) {
			return;
		}
		final String SAVE = "UPDATE duoelo SET " + ladder.getName().toLowerCase() + "=?, global=? WHERE uuid1=? AND uuid2=?";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(SAVE);
			
			statement.setInt(1, party.getPartyEloManager().getFrom(ladder));
			statement.setInt(2, party.getPartyEloManager().getGlobal());
			statement.setString(3, party.getLeader().toString());
			statement.setString(4, party.getPartner().toString());
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
		final Map<UUID, Integer> map = new LinkedHashMap<UUID, Integer>(10);
		final String selectLine = "SELECT uuid," + ladder.getName().toLowerCase() + " FROM elo ORDER BY " + ladder.getName().toLowerCase() + " DESC LIMIT 10";
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
		if (!isConnected()) {
			return null;
		}
		final Map<UUID, Integer> map = new LinkedHashMap<UUID, Integer>(10);
		final String selectLine = "SELECT uuid,global FROM elo ORDER BY global DESC LIMIT 10";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(selectLine);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				final UUID uuid = UUID.fromString(result.getString("uuid"));
				final int elo = result.getInt("global");
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
	
	public Map<UUID, PartnerCache> getDuoTopEloLadder(Ladders ladder) {
		if (!isConnected()) {
			return null;
		}
		final Map<UUID, PartnerCache> map = new LinkedHashMap<UUID, PartnerCache>(10);
		final String selectLine = "SELECT uuid1,uuid2," + ladder.getName().toLowerCase() + " FROM duoelo ORDER BY " + ladder.getName().toLowerCase() + " DESC LIMIT 10";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(selectLine);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				final UUID uuid1 = UUID.fromString(result.getString("uuid1"));
				final UUID uuid2 = UUID.fromString(result.getString("uuid2"));
				final int elo = result.getInt(ladder.getName().toLowerCase());
				map.put(uuid1, new PartnerCache(uuid2, elo));
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
	public Map<UUID, PartnerCache> getDuoGlobalTopElo() {
		if (!isConnected()) {
			return null;
		}
		final Map<UUID, PartnerCache> map = new LinkedHashMap<UUID, PartnerCache>(10);
		final String selectLine = "SELECT uuid1,uuid2,global FROM duoelo ORDER BY global DESC LIMIT 10";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(selectLine);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				final UUID uuid1 = UUID.fromString(result.getString("uuid1"));
				final UUID uuid2 = UUID.fromString(result.getString("uuid2"));
				final int elo = result.getInt("global");
				map.put(uuid1, new PartnerCache(uuid2, elo));
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
	
	public EloManager loadOrCreateDuo(UUID uuid1, UUID uuid2) {
		EloManager elo = new EloManager();
		if (!isConnected()) {
			return elo;
		}
		final StringJoiner questionMarks = new StringJoiner(", ");
		for (int i = 0; i < Ladders.values().length; i++) {
			questionMarks.add("?");
		}
		final String insertLine = "INSERT INTO duoelo VALUES(?, ?, " + questionMarks.toString() + ", ?) ON DUPLICATE KEY UPDATE uuid1=?";
		final String selectLine = "SELECT * FROM duoelo WHERE uuid1=? AND uuid2=?";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(insertLine);
			statement.setString(1, uuid1.toString());
			statement.setString(2, uuid2.toString());
			int i = 2;
			while (i != Ladders.values().length + 2) {
				i++;
				statement.setInt(i, 1200);
			}
			statement.setInt(i + 1, 1200);
			statement.setString(i + 2, uuid1.toString());
			statement.executeUpdate();
			statement.close();
			
			statement = connection.prepareStatement(selectLine);
			statement.setString(1, uuid1.toString());
			statement.setString(2, uuid2.toString());
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				final List<Integer> elos = Lists.newArrayList();
				for (Ladders ladders : Ladders.values()) {
					elos.add(result.getInt(ladders.getName().toLowerCase()));
				}
				elo = new EloManager(elos);
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
		return elo;
	}
	
	public HikariDataSource getHikari() {
		return this.hikari;
	}

	public boolean isConnected() {
		return this.connected;
	}
	
	/*
	Player player = ... // get the player
	Inventory inventory = player.getInventory();
	
	byte[] inventoryBytes = serializeInventory(inventory);
	
	private byte[] serializeInventory(Inventory inventory) throws IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
    	dataOutput.writeInt(inventory.getSize());
    	for (int i = 0; i < inventory.getSize(); i++) {
        	dataOutput.writeObject(inventory.getItem(i));
    	}
    	dataOutput.close();
    	return outputStream.toByteArray();
	}
	
	PreparedStatement stmt = connection.prepareStatement("INSERT INTO player_inventory (player_name, inventory) VALUES (?, ?)");
	stmt.setString(1, player.getName());
	stmt.setBytes(2, inventoryBytes);
	stmt.executeUpdate();
	
	PreparedStatement stmt = connection.prepareStatement("SELECT inventory FROM player_inventory WHERE player_name = ?");
	stmt.setString(1, player.getName());
	ResultSet rs = stmt.executeQuery();
	if (rs.next()) {
    	byte[] inventoryBytes = rs.getBytes("inventory");
	    Inventory inventory = deserializeInventory(inventoryBytes);
	    player.getInventory().setContents(inventory.getContents()); // set the player's inventory from the retrieved inventory
	}
	
	private Inventory deserializeInventory(byte[] inventoryBytes) throws IOException, ClassNotFoundException {
    	ByteArrayInputStream inputStream = new ByteArrayInputStream(inventoryBytes);
    	BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
    	int size = dataInput.readInt();
    	Inventory inventory = Bukkit.getServer().createInventory(null, size);
    	for (int i = 0; i < size; i++) {
        	inventory.setItem(i, (ItemStack) dataInput.readObject());
    	}
    	dataInput.close();
    	return inventory;
	}
	
	 */
}
