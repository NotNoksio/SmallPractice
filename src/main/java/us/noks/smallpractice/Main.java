package us.noks.smallpractice;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import us.noks.smallpractice.commands.AcceptCommand;
import us.noks.smallpractice.commands.BuildCommand;
import us.noks.smallpractice.commands.DuelCommand;
import us.noks.smallpractice.commands.ForceDuelCommand;
import us.noks.smallpractice.commands.InventoryCommand;
import us.noks.smallpractice.commands.ModerationCommand;
import us.noks.smallpractice.commands.PartyCommand;
import us.noks.smallpractice.commands.PingCommand;
import us.noks.smallpractice.commands.ReportCommand;
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
	
	private static Main instance;
	public static Main getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		instance = this;
		registerCommands();
		registerListers();
		getPermissionsPlugin();
	}
	
	@Override
	public void onDisable() {
		QueueManager.getInstance().getQueue().clear();
	}
	
	private void registerCommands() {
		getCommand("duel").setExecutor(new DuelCommand());
		getCommand("accept").setExecutor(new AcceptCommand());
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
	}
	
	private void registerListers() {
		new PlayerListener(this);
		new ServerListeners(this);
		new EnderDelay();
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
}
