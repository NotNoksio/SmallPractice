package io.noks.smallpractice;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import io.noks.smallpractice.commands.AcceptCommand;
import io.noks.smallpractice.commands.BuildCommand;
import io.noks.smallpractice.commands.DenyCommand;
import io.noks.smallpractice.commands.DuelCommand;
import io.noks.smallpractice.commands.EloCommand;
import io.noks.smallpractice.commands.ForceDuelCommand;
import io.noks.smallpractice.commands.InventoryCommand;
import io.noks.smallpractice.commands.MentionCommand;
import io.noks.smallpractice.commands.ModerationCommand;
import io.noks.smallpractice.commands.NameMCCommand;
import io.noks.smallpractice.commands.PartyCommand;
import io.noks.smallpractice.commands.PingCommand;
import io.noks.smallpractice.commands.PlayerTimeCommand;
import io.noks.smallpractice.commands.ReportCommand;
import io.noks.smallpractice.commands.ResetTimeCommand;
import io.noks.smallpractice.commands.RollCommand;
import io.noks.smallpractice.commands.SeeallCommand;
import io.noks.smallpractice.commands.SpawnCommand;
import io.noks.smallpractice.commands.SpectateCommand;
import io.noks.smallpractice.enums.PlayerTimeEnum;
import io.noks.smallpractice.listeners.ChatListener;
import io.noks.smallpractice.listeners.DuelListener;
import io.noks.smallpractice.listeners.EnderDelay;
import io.noks.smallpractice.listeners.InventoryListener;
import io.noks.smallpractice.listeners.PlayerListener;
import io.noks.smallpractice.listeners.ServerListeners;
import io.noks.smallpractice.objects.Duel;
import io.noks.smallpractice.objects.managers.ConfigManager;
import io.noks.smallpractice.objects.managers.DuelManager;
import io.noks.smallpractice.objects.managers.InventoryManager;
import io.noks.smallpractice.objects.managers.ItemManager;
import io.noks.smallpractice.objects.managers.PartyManager;
import io.noks.smallpractice.objects.managers.QueueManager;
import io.noks.smallpractice.objects.managers.RequestManager;
import io.noks.smallpractice.utils.DBUtils;

public class Main extends JavaPlugin {
	private Map<UUID, Inventory> offlineInventories = new WeakHashMap<UUID, Inventory>();
	private DuelManager duelManager;
	private ItemManager itemManager;
	private InventoryManager inventoryManager;
	private ConfigManager configManager;
	private QueueManager queueManager;
	private RequestManager requestManager;
	private PartyManager partyManager;
	private DBUtils database;
	
	private static Main instance;
	public static Main getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		instance = this;
		
		this.database = new DBUtils();
		//this.database.connectDatabase();
		
		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
		
		this.partyManager = new PartyManager();
		this.duelManager = new DuelManager();
		this.itemManager = new ItemManager();
		this.configManager = new ConfigManager(this);
		this.queueManager = new QueueManager();
		this.requestManager = new RequestManager();
		this.inventoryManager = new InventoryManager();
		
		registerCommands();
		registerListers();
	}
	
	@Override
	public void onDisable() {
		this.queueManager.getQueueMap().clear();
		this.offlineInventories.clear();
		for (Duel duels : this.duelManager.getAllDuels()) {
			if (duels == null) continue;
			this.duelManager.finishDuel(duels, true);
		}
	}
	
	private void registerCommands() {
		getCommand("duel").setExecutor(new DuelCommand());
		getCommand("accept").setExecutor(new AcceptCommand());
		getCommand("deny").setExecutor(new DenyCommand());
		getCommand("build").setExecutor(new BuildCommand());
		getCommand("ping").setExecutor(new PingCommand());
		getCommand("inventory").setExecutor(new InventoryCommand());
		getCommand("spawn").setExecutor(new SpawnCommand());
		getCommand("seeall").setExecutor(new SeeallCommand());
		getCommand("report").setExecutor(new ReportCommand());
		getCommand("spectate").setExecutor(new SpectateCommand());
		getCommand("mod").setExecutor(new ModerationCommand());
		getCommand("party").setExecutor(new PartyCommand());
		getCommand("forceduel").setExecutor(new ForceDuelCommand());
		for (PlayerTimeEnum pte : PlayerTimeEnum.values()) {
			getCommand(pte.getName().toLowerCase()).setExecutor(new PlayerTimeCommand());
		}
		getCommand("resettime").setExecutor(new ResetTimeCommand());
		getCommand("roll").setExecutor(new RollCommand());
		getCommand("mention").setExecutor(new MentionCommand());
		getCommand("namemc").setExecutor(new NameMCCommand());
		getCommand("elo").setExecutor(new EloCommand());
	}
	
	private void registerListers() {
		new PlayerListener(this);
		new ServerListeners(this);
		Bukkit.getServer().getPluginManager().registerEvents(new EnderDelay(), this);
		new ChatListener(this);
		new DuelListener(this);
		new InventoryListener(this);
	}
	
	public Map<UUID, Inventory> getOfflineInventories() {
		return this.offlineInventories;
	}
	
	public DuelManager getDuelManager() {
		return this.duelManager;
	}
	
	public ItemManager getItemManager() {
		return this.itemManager;
	}
	
	public InventoryManager getInventoryManager() {
		return this.inventoryManager;
	}
	
	public ConfigManager getConfigManager() {
		return this.configManager;
	}
	
	public QueueManager getQueueManager() {
		return this.queueManager;
	}
	
	public RequestManager getRequestManager() {
		return this.requestManager;
	}
	
	public PartyManager getPartyManager() {
		return this.partyManager;
	}
	
	public DBUtils getDatabaseUtil() {
		return this.database;
	}
}
