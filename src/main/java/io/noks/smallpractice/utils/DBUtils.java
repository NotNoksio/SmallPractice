package io.noks.smallpractice.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.zaxxer.hikari.HikariDataSource;

import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.objects.EditedLadderKit;
import io.noks.smallpractice.objects.PlayerSettings;
import io.noks.smallpractice.objects.managers.EloManager;
import io.noks.smallpractice.objects.managers.PlayerManager;
import io.noks.smallpractice.party.Party;
import net.minecraft.util.com.google.common.collect.Lists;

public class DBUtils {
	private Map<String, Map<UUID, Integer>> soloTop = new HashMap<String, Map<UUID, Integer>>(Ladders.values().length + 1);
	private Map<String, Map<UUID, PartnerCache>> duoTop = new HashMap<String, Map<UUID, PartnerCache>>(Ladders.values().length + 1);
	private boolean connected = false;
	private final String address;
	private final String name;
	private final String username;
	private final String password;

	private HikariDataSource hikari;
	
	private final String SAVE_ELO;
	private final String INSERT_ELO;
	private final String INSERT_DUOELO;
	public DBUtils(String address, String name, String user, String password) {
		this.address = address;
		this.name = name;
		this.username = user;
		this.password = password;
		this.connectDatabase();
		final StringJoiner ladder = new StringJoiner(", ");
		final StringJoiner questionMarks = new StringJoiner(", ");
		for (Ladders ladders : Ladders.values()) {
			ladder.add(ladders.getName().toLowerCase() + "=?");
			questionMarks.add("?");
			updateSoloTopLadder(ladders, ladders == Ladders.NODEBUFF);
			updateDuoTopLadder(ladders, ladders == Ladders.NODEBUFF);
		}
		this.SAVE_ELO = "UPDATE elo SET " + ladder.toString() + ", global=?, unrankedwin=? WHERE uuid=?";
		this.INSERT_ELO = "INSERT INTO elo VALUES(?, " + questionMarks.toString() + ", ?, ?) ON DUPLICATE KEY UPDATE uuid=?";
		this.INSERT_DUOELO = "INSERT INTO duoelo VALUES(?, ?, " + questionMarks.toString() + ", ?) ON DUPLICATE KEY UPDATE uuid1=?";
	}
	public void clearCache() {
		this.soloTop.clear();
		this.duoTop.clear();
	}
	
	public void updateSoloTopLadder(Ladders ladder, boolean globalUpdate) {
		this.soloTop.put(ladder.getName(), getTopEloLadder(ladder));
		if (globalUpdate) {
			this.soloTop.put("Global", getGlobalTopElo());
		}
	}
	public void updateDuoTopLadder(Ladders ladder, boolean globalUpdate) {
		this.duoTop.put(ladder.getName(), getDuoTopEloLadder(ladder));
		if (globalUpdate) {
			this.duoTop.put("Global", getDuoGlobalTopElo());
		}
	}
	
	public void connectDatabase() {
		if (this.address.length() == 0 || this.name.length() == 0 || this.username.length() == 0 || this.password.length() == 0) {
			return;
		}
		try {
			this.hikari = new HikariDataSource();
			this.hikari.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
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
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS settings (uuid varchar(36), pingdiff int(3), tpm TINYINT(1), invite TINYINT(1), request TINYINT(1), requestdelay int(2), scoreboard TINYINT(1), PRIMARY KEY(`uuid`), UNIQUE(`uuid`));");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS duoelo (uuid1 varchar(36), uuid2 varchar(36), " + ladder.toString() + ", global int(4), PRIMARY KEY(uuid1, uuid2), CONSTRAINT unique_uuid_pair UNIQUE KEY(uuid1, uuid2), KEY(uuid1, uuid2));");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS customkits (uuid varchar(36), ladder varchar(16), slot int(1), name varchar(28), inventory LONGBLOB, PRIMARY KEY(uuid, ladder, slot));");
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

	private final String SELECT_ELO = "SELECT * FROM elo WHERE uuid=?";
	private final String INSERT_SETTINGS = "INSERT INTO settings VALUES(?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid=?";
	private final String SELECT_SETTINGS = "SELECT * FROM settings WHERE uuid=?";
	private final String SELECT_KITS = "SELECT ladder, slot, name, inventory FROM customkits WHERE uuid=?";
	public void loadPlayer(UUID uuid) {
		if (!isConnected()) {
			new PlayerManager(uuid).heal(false);
			return;
		}
		Connection connection = null;
		EloManager elo = new EloManager();
		PlayerSettings settings = new PlayerSettings();
		final List<EditedLadderKit> customKits = Lists.newArrayList();
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
			statement.setInt(6, 5);
			statement.setBoolean(7, true);
			statement.setString(8, uuid.toString());
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
			statement.close();
			
			statement = connection.prepareStatement(SELECT_SETTINGS);
			statement.setString(1, uuid.toString());
			result = statement.executeQuery();
			if (result.next()) {
				settings = new PlayerSettings(result.getInt("pingdiff"), result.getBoolean("tpm"), result.getBoolean("invite"), result.getBoolean("request"), result.getInt("requestdelay"), result.getBoolean("scoreboard"));
			}
			result.close();
			statement.close();
			
			statement = connection.prepareStatement(SELECT_KITS);
			statement.setString(1, uuid.toString());
			result = statement.executeQuery();
			while(result.next()) {
				customKits.add(new EditedLadderKit(Ladders.getLadderFromName(result.getString("ladder")), result.getString("name"), result.getInt("slot"), this.deserializeInventory(result.getBytes("inventory"))));
			}
			result.close();
			statement.close();
		} catch (SQLException | ClassNotFoundException | IOException ex) {
			ex.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			new PlayerManager(uuid, elo, settings, customKits);
		}
	}

	private final String SAVE_SETTINGS = "UPDATE settings SET pingdiff=?, tpm=?, invite=?, request=?, requestdelay=?, scoreboard=? WHERE uuid=?";
	private final String INSERT_KITS = "INSERT INTO customkits VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=?, inventory=?";
	public void savePlayer(PlayerManager pm) {
		if (!isConnected()) {
			pm.remove();
			return;
		}
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
			statement.setInt(5, pm.getSettings().getSecondsBeforeRerequest());
			statement.setBoolean(6, pm.getSettings().isScoreboardToggled());
			statement.setString(7, pm.getPlayerUUID().toString());
			statement.execute();
			statement.close();
			
			if (!pm.getCustomLadderKitList().isEmpty()) {
				statement = connection.prepareStatement(INSERT_KITS);
				for (EditedLadderKit customKits : pm.getCustomLadderKitList()) {
					statement.setString(1, pm.getPlayerUUID().toString());
					statement.setString(2, customKits.getLadder().getName());
					statement.setInt(3, customKits.getSlot());
					statement.setString(4, customKits.getName());
					statement.setBytes(5, this.serializeInventory(customKits.getInventory()));
					statement.setString(6, customKits.getName());
					statement.setBytes(7, this.serializeInventory(customKits.getInventory()));
					statement.executeUpdate();
				}
				statement.close();
			}
		} catch (SQLException | IOException e) {
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
			final PreparedStatement statement = connection.prepareStatement(SAVE);
			
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
		final UUID uuid1 = (isDuoExist(party.getLeader(), party.getPartner()) ? party.getLeader() : party.getPartner());
		final UUID uuid2 = (isDuoExist(party.getLeader(), party.getPartner()) ? party.getPartner() : party.getLeader());
		try {
			connection = this.hikari.getConnection();
			final PreparedStatement statement = connection.prepareStatement(SAVE);
			
			statement.setInt(1, party.getPartyEloManager().getFrom(ladder));
			statement.setInt(2, party.getPartyEloManager().getGlobal());
			statement.setString(3, uuid1.toString());
			statement.setString(4, uuid2.toString());
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
	
	public Map<UUID, Integer> getTopEloLadderList(Ladders ladder){
		return this.soloTop.get(ladder.getName());
	}
	private Map<UUID, Integer> getTopEloLadder(Ladders ladder) {
		if (!isConnected()) {
			return null;
		}
		final Map<UUID, Integer> map = new LinkedHashMap<UUID, Integer>(10);
		final String selectLine = "SELECT uuid," + ladder.getName().toLowerCase() + " FROM elo ORDER BY " + ladder.getName().toLowerCase() + " DESC LIMIT 10";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			final PreparedStatement statement = connection.prepareStatement(selectLine);
			final ResultSet result = statement.executeQuery();
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
		return map.isEmpty() ? Collections.emptyMap() : map;
	}
	public Map<UUID, Integer> getGlobalTopEloList(){
		return this.soloTop.get("Global");
	}
	private final String SELECT_GLOBAL_TOP = "SELECT uuid,global FROM elo ORDER BY global DESC LIMIT 10";
	private Map<UUID, Integer> getGlobalTopElo() {
		if (!isConnected()) {
			return null;
		}
		final Map<UUID, Integer> map = new LinkedHashMap<UUID, Integer>(10);
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			final PreparedStatement statement = connection.prepareStatement(this.SELECT_GLOBAL_TOP);
			final ResultSet result = statement.executeQuery();
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
		return map.isEmpty() ? Collections.emptyMap() : map;
	}
	
	public Map<UUID, PartnerCache> getDuoTopEloLadderList(Ladders ladder){
		return this.duoTop.get(ladder.getName());
	}
	private Map<UUID, PartnerCache> getDuoTopEloLadder(Ladders ladder) {
		if (!isConnected()) {
			return null;
		}
		final Map<UUID, PartnerCache> map = new LinkedHashMap<UUID, PartnerCache>(10);
		final String selectLine = "SELECT uuid1,uuid2," + ladder.getName().toLowerCase() + " FROM duoelo ORDER BY " + ladder.getName().toLowerCase() + " DESC LIMIT 10";
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			final PreparedStatement statement = connection.prepareStatement(selectLine);
			final ResultSet result = statement.executeQuery();
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
		return map.isEmpty() ? Collections.emptyMap() : map;
	}
	public Map<UUID, PartnerCache> getDuoGlobalTopEloList(){
		return this.duoTop.get("Global");
	}
	private final String SELECT_GLOBAL_DUO_TOP = "SELECT uuid1,uuid2,global FROM duoelo ORDER BY global DESC LIMIT 10";
	private Map<UUID, PartnerCache> getDuoGlobalTopElo() {
		if (!isConnected()) {
			return null;
		}
		final Map<UUID, PartnerCache> map = new LinkedHashMap<UUID, PartnerCache>(10);
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			final PreparedStatement statement = connection.prepareStatement(this.SELECT_GLOBAL_DUO_TOP);
			final ResultSet result = statement.executeQuery();
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
		return map.isEmpty() ? Collections.emptyMap() : map;
	}
	
	private final String SELECT_DUOELO = "SELECT * FROM duoelo WHERE uuid1=? AND uuid2=?";
	public EloManager loadOrCreateDuo(UUID uuid1, UUID uuid2) {
		EloManager elo = new EloManager();
		if (!isConnected()) {
			return elo;
		}
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			PreparedStatement statement = connection.prepareStatement(this.INSERT_DUOELO);
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
			
			statement = connection.prepareStatement(this.SELECT_DUOELO);
			statement.setString(1, uuid1.toString());
			statement.setString(2, uuid2.toString());
			final ResultSet result = statement.executeQuery();
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
	
	private final String DELETE_KIT = "DELETE FROM customkits WHERE uuid=? AND ladder=? AND slot=?";
	public void deleteCustomKit(UUID uuid, Ladders ladder, int slot) {
		if (!isConnected()) {
			return;
		}
		if (!isCustomKitLadderSlotExist(uuid, ladder, slot)) {
			return;
		}
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			final PreparedStatement statement = connection.prepareStatement(this.DELETE_KIT);
			statement.setString(1, uuid.toString());
			statement.setString(2, ladder.getName());
			statement.setInt(3, slot);
			statement.executeUpdate();
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
	private final String CUSTOM_KIT_EXIST = "SELECT COUNT(*) FROM customkits WHERE uuid=? AND ladder=? AND slot=?";
	private boolean isCustomKitLadderSlotExist(UUID uuid, Ladders ladder, int slot) {
		if (!isConnected()) {
			return false;
		}
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			final PreparedStatement statement = connection.prepareStatement(this.CUSTOM_KIT_EXIST);
			statement.setString(1, uuid.toString());
			statement.setString(2, ladder.getName());
			statement.setInt(3, slot);
			final ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
            	int count = resultSet.getInt(1);
            	return count == 1;
            }
            resultSet.close();
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
		return false;
	}
	
	private final String SELECT_DUO_EXIST = "SELECT COUNT(*) FROM duoelo WHERE uuid1=? AND uuid2=?";
	public boolean isDuoExist(UUID uuid1, UUID uuid2) {
		if (!isConnected()) {
			return false;
		}
		Connection connection = null;
		try {
			connection = this.hikari.getConnection();
			final PreparedStatement statement = connection.prepareStatement(this.SELECT_DUO_EXIST);
			statement.setString(1, uuid1.toString());
			statement.setString(2, uuid2.toString());
			final ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
            	int count = resultSet.getInt(1);
            	return count == 1;
            }
            resultSet.close();
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
		return false;
	}
	
	public HikariDataSource getHikari() {
		return this.hikari;
	}

	public boolean isConnected() {
		return this.connected;
	}
	
	private byte[] serializeInventory(Inventory inventory) throws IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
    	for (ItemStack items : inventory.getContents()) {
    		dataOutput.writeObject(items);
    	}
    	dataOutput.close();
    	return outputStream.toByteArray();
	}
	
	private Inventory deserializeInventory(byte[] inventoryBytes) throws IOException, ClassNotFoundException {
    	ByteArrayInputStream inputStream = new ByteArrayInputStream(inventoryBytes);
    	BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
    	Inventory inventory = Bukkit.getServer().createInventory(null, InventoryType.PLAYER);
    	for (int i = 0; i < inventory.getSize(); i++) {
        	inventory.setItem(i, (ItemStack) dataInput.readObject());
    	}
    	dataInput.close();
    	return inventory;
	}
}
