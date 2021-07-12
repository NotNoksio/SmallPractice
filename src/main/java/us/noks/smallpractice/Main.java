package us.noks.smallpractice;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import us.noks.smallpractice.commands.AcceptCommand;
import us.noks.smallpractice.commands.BuildCommand;
import us.noks.smallpractice.commands.DayCommand;
import us.noks.smallpractice.commands.DenyCommand;
import us.noks.smallpractice.commands.DuelCommand;
import us.noks.smallpractice.commands.ForceDuelCommand;
import us.noks.smallpractice.commands.InventoryCommand;
import us.noks.smallpractice.commands.MentionCommand;
import us.noks.smallpractice.commands.ModerationCommand;
import us.noks.smallpractice.commands.NameMCCommand;
import us.noks.smallpractice.commands.NightCommand;
import us.noks.smallpractice.commands.PartyCommand;
import us.noks.smallpractice.commands.PingCommand;
import us.noks.smallpractice.commands.ReportCommand;
import us.noks.smallpractice.commands.ResetTimeCommand;
import us.noks.smallpractice.commands.RollCommand;
import us.noks.smallpractice.commands.SeeallCommand;
import us.noks.smallpractice.commands.SpawnCommand;
import us.noks.smallpractice.commands.SpectateCommand;
import us.noks.smallpractice.listeners.ChatListener;
import us.noks.smallpractice.listeners.DuelListener;
import us.noks.smallpractice.listeners.EnderDelay;
import us.noks.smallpractice.listeners.InventoryListener;
import us.noks.smallpractice.listeners.PlayerListener;
import us.noks.smallpractice.listeners.ServerListeners;
import us.noks.smallpractice.objects.managers.QueueManager;

public class Main extends JavaPlugin {
	private boolean permissionsPluginHere;
	private Map<UUID, Inventory> offlineInventory = new WeakHashMap<UUID, Inventory>();
	public boolean clearChatOnJoin = true;
	
	private static Main instance;
	public static Main getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		instance = this;
		clearChatOnJoin = this.getConfig().getBoolean("clear-chat-on-join");
		registerCommands();
		registerListers();
		getPermissionsPlugin();
		System.out.println("PermissionsEx is " + (permissionsPluginHere ? "" : "not ") + "present");
	}
	
	@Override
	public void onDisable() {
		QueueManager.getInstance().getQueueMap().clear();
		this.offlineInventory.clear();
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
		getCommand("day").setExecutor(new DayCommand());
		getCommand("night").setExecutor(new NightCommand());
		getCommand("resettime").setExecutor(new ResetTimeCommand());
		getCommand("roll").setExecutor(new RollCommand());
		getCommand("mention").setExecutor(new MentionCommand());
		getCommand("namemc").setExecutor(new NameMCCommand());
	}
	
	private void registerListers() {
		new PlayerListener(this);
		new ServerListeners(this);
		Bukkit.getServer().getPluginManager().registerEvents(new EnderDelay(), this);
		new ChatListener(this);
		new DuelListener(this);
		new InventoryListener(this);
	}
	
	private void getPermissionsPlugin() {
		if (hasPermissionsPlugin()) {
			this.permissionsPluginHere = true;
			return;
		}
		this.permissionsPluginHere = false;
	}
	
	private boolean hasPermissionsPlugin() {
		return Bukkit.getPluginManager().getPlugin("PermissionsEx") != null;
	}
	
	public boolean isPermissionsPluginHere() {
		return this.permissionsPluginHere;
	}
	
	public Map<UUID, Inventory> getOfflineInventoryMap() {
		return this.offlineInventory;
	}
}
