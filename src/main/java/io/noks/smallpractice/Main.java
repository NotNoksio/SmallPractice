package io.noks.smallpractice;

import java.util.Iterator;

import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;

import io.noks.smallpractice.arena.ArenaManager;
import io.noks.smallpractice.commands.ArenasCommand;
import io.noks.smallpractice.commands.BuildCommand;
import io.noks.smallpractice.commands.DecisionsCommand;
import io.noks.smallpractice.commands.DuelCommand;
import io.noks.smallpractice.commands.ForceDuelCommand;
import io.noks.smallpractice.commands.FreezeCommand;
import io.noks.smallpractice.commands.InventoryCommand;
import io.noks.smallpractice.commands.MentionCommand;
import io.noks.smallpractice.commands.ModerationCommand;
import io.noks.smallpractice.commands.NameMCCommand;
import io.noks.smallpractice.commands.PartyCommand;
import io.noks.smallpractice.commands.PingCommand;
import io.noks.smallpractice.commands.PlayerTimeCommand;
import io.noks.smallpractice.commands.ReportCommand;
import io.noks.smallpractice.commands.RollCommand;
import io.noks.smallpractice.commands.SeeallCommand;
import io.noks.smallpractice.commands.SpawnCommand;
import io.noks.smallpractice.commands.SpectateCommand;
import io.noks.smallpractice.commands.StatsCommand;
import io.noks.smallpractice.enums.PlayerTimeEnum;
import io.noks.smallpractice.listeners.ChatListener;
import io.noks.smallpractice.listeners.DuelListener;
import io.noks.smallpractice.listeners.EnderDelay;
import io.noks.smallpractice.listeners.InventoryListener;
import io.noks.smallpractice.listeners.PlayerListener;
import io.noks.smallpractice.listeners.ServerListeners;
import io.noks.smallpractice.objects.duel.Duel;
import io.noks.smallpractice.objects.managers.ConfigManager;
import io.noks.smallpractice.objects.managers.DuelManager;
import io.noks.smallpractice.objects.managers.InventoryManager;
import io.noks.smallpractice.objects.managers.ItemManager;
import io.noks.smallpractice.objects.managers.PlayerManager;
import io.noks.smallpractice.objects.managers.QueueManager;
import io.noks.smallpractice.objects.managers.RequestManager;
import io.noks.smallpractice.party.PartyManager;
import io.noks.smallpractice.utils.DBUtils;

public class Main extends JavaPlugin {
	private ArenaManager arenaManager;
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
		this.database = new DBUtils(getConfig().getString("database.address"), getConfig().getString("database.name"), getConfig().getString("database.username"), getConfig().getString("database.password"));
		
		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
		
		this.arenaManager = new ArenaManager();
		this.partyManager = new PartyManager(this);
		this.duelManager = new DuelManager(this);
		this.itemManager = new ItemManager(this);
		this.configManager = new ConfigManager(this);
		this.queueManager = new QueueManager(this);
		this.requestManager = new RequestManager(this);
		this.inventoryManager = new InventoryManager(this);
		
		//this.arenaManager.getFullArenaList().get(0).getWorld().spawnNPC(UUID.randomUUID(), "N0KS10", "ewogICJ0aW1lc3RhbXAiIDogMTY5NzQzMDY1NjUxOCwKICAicHJvZmlsZUlkIiA6ICIzNWIxMjg0OWYxYTY0YTc4YTM0ZTMyMzc5NjIxOGNmMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb2tzaW8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTBmYjhkZmMyNTliZmIxYzBlZGIzNGFlYmNlMmVkZjQ2YmFjZWEzNTQ2ODllOTQ1ZDFkMDAwNWM5NWRhZmMwZCIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM0MGMwZTAzZGQyNGExMWIxNWE4YjMzYzJhN2U5ZTMyYWJiMjA1MWIyNDgxZDBiYTdkZWZkNjM1Y2E3YTkzMyIKICAgIH0KICB9Cn0=", "Edwcaev9uctdzc8q2r3SOfn4It5x735SNf4tHP7kZJ3hGY7F+Aa/9kXkMVAbBokWWkQ71ZdXbg7K4NRK0++gu4OJ8WBaPLYVA5GXzBpYS4XMChitweEkqoeV58Gb4F4FxOEdbRckwnGXEyCqC8lobLXhz1sOn7e5DC7DqX0GElS3dp4RpzV7jWrqVeMTw1dRZZ2Wfqr2rixf5zWEagJSPyzh9hqZPbp6wygZToLya9w+7jzfgM0QOD8M85N+awRuAU90VVcvB2TG4ekB6SjYqt+GC0YTD/Yoy2wY4R9Nf5v6Vc1M+VXITqtKQ2Ie5vsmJaDc0X+fkGZrFNj+5HPo5qXbW/BITGhwVRooOF5tI2xv+ecK0suj7XWRk34yu/eJSf2z4rNod6YZaaVjm6NCJtlOuklUC4aXScjIXdr1a5ag49TaWa/JC8yDdkmKCWTADKzrl0MotarIchUL6SZPZMCw+mO+yzW0qyYs5WlBaMo9B4p9MYRSba3+xtTd2m0ZIvty1JhVfUDOH+wQ35nDmemqThJh32DoWas761iiWhSfGnMIxmm/4oifP74jIk9KzrwDmlcZWAgKwsNVfpyb3DypQWWRwZdn/ntdVnFMjorISd+nIDauCV9lD6iZXgd+mH9eZaVjUa2FDyCs3S7bOjDELA6f3SmWzbhtRKCkOSQ=", new Location(this.arenaManager.getFullArenaList().get(0).getWorld(), -15.5D, 77.0D, 65.5D, 180.0F, 0.0F));
		
		registerCommands();
		registerListers();
	}
	
	@Override
	public void onDisable() {
		this.queueManager.clearCache();
		this.inventoryManager.clearCache();
		this.itemManager.clearCache();
		this.database.clearCache();
		if (!this.duelManager.getAllDuels().isEmpty()) {
			Duel lastCheck = null;
			for (Duel duels : this.duelManager.getAllDuels()) {
				if (duels == null || lastCheck == duels) continue;
				lastCheck = duels;
				this.duelManager.finishDuel(duels, true);
			}
		}
		final World world = this.arenaManager.getFullArenaList().get(0).getWorld();
		final Iterator<Entity> it = world.getEntities().iterator();
		while (it.hasNext()) {
			final Entity entities = it.next();
			if (entities == null || !(entities instanceof Item) || !(entities instanceof Arrow)) continue;
			entities.remove();
		}
		if (!PlayerManager.players.isEmpty()) {
			for (PlayerManager pm : PlayerManager.players.values()) {
				this.database.savePlayer(pm);
			}
		}
	}
	
	private void registerCommands() {
		getCommand("duel").setExecutor(new DuelCommand());
		new DecisionsCommand(this);
		getCommand("build").setExecutor(new BuildCommand());
		getCommand("ping").setExecutor(new PingCommand());
		getCommand("inventory").setExecutor(new InventoryCommand());
		getCommand("spawn").setExecutor(new SpawnCommand());
		getCommand("seeall").setExecutor(new SeeallCommand());
		getCommand("report").setExecutor(new ReportCommand());
		new SpectateCommand(this);
		getCommand("mod").setExecutor(new ModerationCommand());
		new PartyCommand(this);
		new ForceDuelCommand(this);
		for (PlayerTimeEnum pte : PlayerTimeEnum.values()) {
			getCommand(pte.getName().toLowerCase()).setExecutor(new PlayerTimeCommand());
		}
		getCommand("resettime").setExecutor(new PlayerTimeCommand());
		getCommand("roll").setExecutor(new RollCommand());
		getCommand("mention").setExecutor(new MentionCommand());
		getCommand("namemc").setExecutor(new NameMCCommand());
		getCommand("stats").setExecutor(new StatsCommand());
		getCommand("arenas").setExecutor(new ArenasCommand());
		getCommand("freeze").setExecutor(new FreezeCommand());
	}
	
	private void registerListers() {
		new PlayerListener(this);
		new ServerListeners(this);
		this.getServer().getPluginManager().registerEvents(new EnderDelay(), this);
		new ChatListener(this);
		new DuelListener(this);
		new InventoryListener(this);
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
	
	public ArenaManager getArenaManager() {
		return this.arenaManager;
	}
}
