package us.noks.smallpractice;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
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
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.listeners.ChatListener;
import us.noks.smallpractice.listeners.DuelListener;
import us.noks.smallpractice.listeners.EnderDelay;
import us.noks.smallpractice.listeners.InventoryListener;
import us.noks.smallpractice.listeners.PlayerListener;
import us.noks.smallpractice.listeners.ServerListeners;
import us.noks.smallpractice.objects.managers.DuelManager;
import us.noks.smallpractice.objects.managers.PartyManager;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.utils.InvView;

public class Main extends JavaPlugin {
	
	private Location arena1_Pos1, arena2_Pos1;
	private Location arena1_Pos2, arena2_Pos2;
	private Location spawnLocation;
	private Inventory roundInventory;
	
	public List<Player> queue = Lists.newArrayList();
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
		this.queue.clear();
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
		pm.registerEvents(new InvView(), this);
		pm.registerEvents(new ChatListener(), this);
		pm.registerEvents(new DuelListener(), this);
		pm.registerEvents(new InventoryListener(), this);
	}

	public void sendDuelRequest(Player requester, Player requested) {
		if (PlayerManager.get(requester.getUniqueId()).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(ChatColor.RED + "Either you or that player is not in spawn!");
			return;
		}
		PlayerManager.get(requester.getUniqueId()).setRequestTo(requested.getUniqueId());
		requester.openInventory(getRoundInventory());
	}
	
	public void acceptDuelRequest(Player requested, Player requester) {
		if (PlayerManager.get(requester.getUniqueId()).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(ChatColor.RED + "Either you or this player is not in spawn!");
			return;
		}
		if (!PlayerManager.get(requester.getUniqueId()).hasRequest(requested.getUniqueId())) {
			requested.sendMessage(ChatColor.RED + "This player doesn't request you to duel!");
			return;
		}
		Party requesterParty = PartyManager.getInstance().getParty(requester.getUniqueId());
        Party requestedParty = PartyManager.getInstance().getParty(requested.getUniqueId());
        if ((requesterParty != null && requestedParty == null) || (requestedParty != null && requesterParty == null)) {
            requested.sendMessage(ChatColor.RED + "Either you or this player is in a party!");
            return;
        }
		if (requestedParty != null && requesterParty != null) {
			DuelManager.getInstance().startDuel(requester.getUniqueId(), requested.getUniqueId(), requesterParty.getAllMembersOnline(), requestedParty.getAllMembersOnline(), false, PlayerManager.get(requester.getUniqueId()).getRequestedRound());
			return;
		}
		List<UUID> firstTeam = Lists.newArrayList();
		firstTeam.add(requester.getUniqueId());
		List<UUID> secondTeam = Lists.newArrayList();
		secondTeam.add(requested.getUniqueId());
		
		DuelManager.getInstance().startDuel(null, null, firstTeam, secondTeam, false, PlayerManager.get(requester.getUniqueId()).getRequestedRound());
	}
	
	public void addQueue(Player player, boolean ranked) {
		if (PlayerManager.get(player.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			return;
		}
		if (!this.queue.contains(player)) {
			this.queue.add(player);
			PlayerManager.get(player.getUniqueId()).setStatus(PlayerStatus.QUEUE);
			if (this.queue.size() == 1) {
				PlayerManager.get(player.getUniqueId()).giveQueueItem();
			}
			player.sendMessage(ChatColor.GREEN + "You have been added to the queue. Waiting for another player..");
		}
		if (this.queue.size() == 1 && this.queue.contains(player)) {
			addQueue(player, ranked);
		} else if (this.queue.size() == 2) {
			Player first = this.queue.get(0);
			Player second = this.queue.get(1);
			
			if (first == player && second == first) {
				this.queue.clear();
				addQueue(player, ranked);
				return;
			}
			List<UUID> firstTeam = Lists.newArrayList();
			firstTeam.add(first.getUniqueId());
			List<UUID> secondTeam = Lists.newArrayList();
			secondTeam.add(second.getUniqueId());
			
			DuelManager.getInstance().startDuel(null, null, firstTeam, secondTeam, ranked, 1);
			this.queue.remove(first);
			this.queue.remove(second);
		}
	}
	
	public void quitQueue(Player player) {
		if (this.queue.contains(player)) {
			this.queue.remove(player);
			PlayerManager.get(player.getUniqueId()).setStatus(PlayerStatus.SPAWN);
			PlayerManager.get(player.getUniqueId()).giveSpawnItem();
			player.sendMessage(ChatColor.RED + "You have been removed from the queue.");
		}
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
