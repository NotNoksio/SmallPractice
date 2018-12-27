package us.noks.smallpractice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import us.noks.smallpractice.commands.AcceptCommand;
import us.noks.smallpractice.commands.BuildCommand;
import us.noks.smallpractice.commands.CancelCommand;
import us.noks.smallpractice.commands.DuelCommand;
import us.noks.smallpractice.commands.InventoryCommand;
import us.noks.smallpractice.commands.LeaveCommand;
import us.noks.smallpractice.commands.PingCommand;
import us.noks.smallpractice.commands.RandomCommand;
import us.noks.smallpractice.commands.ReportCommand;
import us.noks.smallpractice.commands.SeeallCommand;
import us.noks.smallpractice.commands.SpawnCommand;
import us.noks.smallpractice.commands.SpectateCommand;
import us.noks.smallpractice.listeners.ChatListener;
import us.noks.smallpractice.listeners.EnderDelay;
import us.noks.smallpractice.listeners.PlayerListener;
import us.noks.smallpractice.listeners.ServerListeners;
import us.noks.smallpractice.objects.managers.DuelManager;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.utils.PlayerStatus;

public class Main extends JavaPlugin {
	
	public Location arena1Pos1, arena2Pos1, arena3Pos1;
	public Location arena1Pos2, arena2Pos2, arena3Pos2;
	public Location spawnLocation;
	
	public List<Player> queue = Lists.newArrayList();
	public Map<Integer, Location[]> arenaList = new HashMap<Integer, Location[]>();
	
	public static Main instance;
	public static Main getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		instance = this;
		
		setupArena();
		
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new ServerListeners(), this);
		Bukkit.getPluginManager().registerEvents(new EnderDelay(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryCommand(), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
		
		getCommand("duel").setExecutor(new DuelCommand());
		getCommand("accept").setExecutor(new AcceptCommand());
		getCommand("build").setExecutor(new BuildCommand());
		getCommand("ping").setExecutor(new PingCommand());
		getCommand("random").setExecutor(new RandomCommand());
		getCommand("cancel").setExecutor(new CancelCommand());
		getCommand("inventory").setExecutor(new InventoryCommand());
		getCommand("spawn").setExecutor(new SpawnCommand());
		getCommand("seeall").setExecutor(new SeeallCommand());
		getCommand("report").setExecutor(new ReportCommand());
		getCommand("spectate").setExecutor(new SpectateCommand());
		getCommand("leave").setExecutor(new LeaveCommand());
	}
	
	@Override
	public void onDisable() {
		this.queue.clear();
		this.arenaList.clear();
	}
	
	public void setupArena() {
		arena1Pos1 = new Location(Bukkit.getWorld("world"), 1977.5, 49, -53.5, 59, 0);
		arena1Pos2 = new Location(Bukkit.getWorld("world"), 1919.5, 49, -18.5, -123, -1);
		arena2Pos1 = new Location(Bukkit.getWorld("world"), 2596.5, 51, -36.5, 90, 0);
		arena2Pos2 = new Location(Bukkit.getWorld("world"), 2508.5, 51, -36.5, -90, 0);
		arena3Pos1 = new Location(Bukkit.getWorld("world"), 3118.5, 51, -44.5, 90, 0);
		arena3Pos2 = new Location(Bukkit.getWorld("world"), 3003.5, 51, -44.5, -90, 0);
		spawnLocation = new Location(Bukkit.getWorld("world"), 0.5, 100.5, 0.5, 180, 0);
		
		arenaList.put(1, new Location[] {arena1Pos1, arena1Pos2});
		arenaList.put(2, new Location[] {arena2Pos1, arena2Pos2});
		arenaList.put(3, new Location[] {arena3Pos1, arena3Pos2});
	}

	public void sendDuelRequest(Player requester, Player requested) {
		if (PlayerManager.get(requester).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(ChatColor.RED + "Either you or that player is not in spawn!");
			return;
		}
		PlayerManager.get(requester).setRequestTo(requested);
		
		TextComponent l1 = new TextComponent();
		l1.setText(requester.getName());
		l1.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
	    
		TextComponent l1a = new TextComponent();
		l1a.setText(" has requested to duel you! ");
		l1a.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
	    
		TextComponent l1b = new TextComponent();
		l1b.setText("Click here to accept.");
		l1b.setColor(net.md_5.bungee.api.ChatColor.GREEN);
		l1b.setBold(true);
		l1b.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GREEN + "Click this message to accept " + requester.getName()).create()));
		l1b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + requester.getName()));
	    
		l1.addExtra(l1a);
		l1.addExtra(l1b);
	    
		requested.spigot().sendMessage(l1);
		requester.sendMessage(ChatColor.DARK_AQUA + "You sent a duel request to " + ChatColor.YELLOW + requested.getName());
	}
	
	public void acceptDuelRequest(Player requested, Player requester) {
		if (PlayerManager.get(requester).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(ChatColor.RED + "Either you or that player is not in spawn!");
			return;
		}
		if (!PlayerManager.get(requester).hasRequest(requested)) {
			requested.sendMessage(ChatColor.RED + "This player doesnt request you to duel!");
			return;
		}
		DuelManager.getInstance().startDuel(requester, requested);
	}
	
	public void addQueue(Player p) {
		if (PlayerManager.get(p).getStatus() != PlayerStatus.SPAWN) {
			return;
		}
		if (!this.queue.contains(p)) {
			this.queue.add(p);
			PlayerManager.get(p).setStatus(PlayerStatus.QUEUE);
			if (this.queue.size() == 1) {
				PlayerManager.get(p).giveQueueItem();
			}
			p.sendMessage(ChatColor.GREEN + "You have been added to the queue. Waiting for another player... " + ChatColor.YELLOW + "Do \"/cancel\" to leave the queue.");
		}
		if (this.queue.size() == 1 && this.queue.contains(p)) {
			addQueue(p);
		} else if (this.queue.size() == 2) {
			Player p1 = this.queue.get(0);
			Player p2 = this.queue.get(1);
			
			if (p1 == p && p2 == p) {
				this.queue.clear();
				addQueue(p);
				return;
			}
			
			DuelManager.getInstance().startDuel(p1, p2);
			this.queue.remove(p1);
			this.queue.remove(p2);
		}
	}
	
	public void quitQueue(Player p) {
		if (this.queue.contains(p)) {
			this.queue.remove(p);
			PlayerManager.get(p).setStatus(PlayerStatus.SPAWN);
			PlayerManager.get(p).giveSpawnItem();
			p.sendMessage(ChatColor.RED + "You have been removed from the queue.");
		}
	}
}
