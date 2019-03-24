package us.noks.smallpractice;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Maps;

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
	
	private Location arena1_Pos1, arena2_Pos1;
	private Location arena1_Pos2, arena2_Pos2;
	private Location spawnLocation;
	private Inventory roundInventory;
	
	public Map<Integer, Location[]> arenaList = Maps.newConcurrentMap();
	
	public static Main instance;
	public static Main getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		instance = this;
		
		setupArena();
		registerCommands();
		registerListers();
		
		this.roundInventory = Bukkit.createInventory(null, InventoryType.HOPPER, "How many rounds?");
		setupRoundInventory();
	}
	
	@Override
	public void onDisable() {
		QueueManager.getInstance().getQueue().clear();
		this.arenaList.clear();
		this.roundInventory.clear();
	}
	
	private void setupArena() {
		arena1_Pos1 = new Location(Bukkit.getWorld("world"), -549.5D, 4.0D, 113.5D, 90.0F, 0.0F);
	    arena1_Pos2 = new Location(Bukkit.getWorld("world"), -608.5D, 4.0D, 115.5D, -90.0F, -1.0F);
	    arena2_Pos1 = new Location(Bukkit.getWorld("world"), 72.5D, 4.0D, 74.5D, 0.0F, 0.0F);
	    arena2_Pos2 = new Location(Bukkit.getWorld("world"), 70.5D, 4.0D, 154.5D, 180.0F, 0.0F);
		spawnLocation = new Location(Bukkit.getWorld("world"), -215.5D, 6.5D, 84.5D, 180.0F, 0.0F);
		
		arenaList.put(1, new Location[] {arena1_Pos1, arena1_Pos2});
		arenaList.put(2, new Location[] {arena2_Pos1, arena2_Pos2});
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
		PluginManager pm = Bukkit.getPluginManager();
		
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new ServerListeners(), this);
		pm.registerEvents(new EnderDelay(), this);
		pm.registerEvents(new ChatListener(), this);
		pm.registerEvents(new DuelListener(), this);
		pm.registerEvents(new InventoryListener(), this);
	}
	
	public Location getSpawnLocation() {
		return this.spawnLocation;
	}
	
	public Inventory getRoundInventory() {
		return this.roundInventory;
	}
	
	private void setupRoundInventory() {
		for(int i = 1; i <= this.roundInventory.getSize(); i++) {
			ItemStack arrow = new ItemStack(Material.ARROW, i);
			ItemMeta am = arrow.getItemMeta();
			am.setDisplayName(ChatColor.YELLOW.toString() + i + " Round" + (i > 1 ? "s" : ""));
			arrow.setItemMeta(am);
			
	        this.roundInventory.setItem(i - 1, arrow);
    	}
	}
}
