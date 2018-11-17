package us.noks.smallpractice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
import us.noks.smallpractice.listeners.EnderDelay;
import us.noks.smallpractice.listeners.PlayerListener;
import us.noks.smallpractice.listeners.ServerListeners;
import us.noks.smallpractice.objects.PlayerManager;
import us.noks.smallpractice.utils.InvView;
import us.noks.smallpractice.utils.PlayerStatus;

public class Main extends JavaPlugin {
	
	private Location arena1Pos1, arena2Pos1, arena3Pos1;
	private Location arena1Pos2, arena2Pos2, arena3Pos2;
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
		
		arena1Pos1 = new Location(Bukkit.getWorld("world"), -549.5, 4, 113.5, 90, 0);
		arena1Pos2 = new Location(Bukkit.getWorld("world"), -608.5, 4, 115.5, -90, -1);
		arena2Pos1 = new Location(Bukkit.getWorld("world"), 72.5, 4, 74.5, 0, 0);
		arena2Pos2 = new Location(Bukkit.getWorld("world"), 70.5, 4, 154.5, 180, 0);
		arena3Pos1 = new Location(Bukkit.getWorld("world"), 272.5, 4, -287.5, -6, 0);
		arena3Pos2 = new Location(Bukkit.getWorld("world"), 281.5, 4, -202.5, 174, 0);
		spawnLocation = new Location(Bukkit.getWorld("world"), -215.5, 6.5, 84.5, 180, 0);
		
		arenaList.put(1, new Location[] {arena1Pos1, arena1Pos2}); // Map Neuker (ClashRoyal)
		arenaList.put(2, new Location[] {arena2Pos1, arena2Pos2}); // Map Noksio (Nimp)
		arenaList.put(3, new Location[] {arena3Pos1, arena3Pos2}); // Map Neuker (EndLolz)
		
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new ServerListeners(), this);
		Bukkit.getPluginManager().registerEvents(new EnderDelay(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryCommand(), this);
		
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
		startDuel(requester, requested);
	}
	
	public void addQueue(Player p) {
		if (PlayerManager.get(p).getStatus() != PlayerStatus.SPAWN) {
			return;
		}
		if (!this.queue.contains(p)) {
			this.queue.add(p);
			PlayerManager.get(p).setStatus(PlayerStatus.QUEUE);
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
			
			startDuel(p1, p2);
			this.queue.remove(p1);
			this.queue.remove(p2);
		}
	}
	
	public void quitQueue(Player p) {
		if (this.queue.contains(p)) {
			this.queue.remove(p);
			PlayerManager.get(p).setStatus(PlayerStatus.SPAWN);
			p.sendMessage(ChatColor.RED + "You have been removed from the queue.");
		}
	}
	
	private void startDuel(Player p1, Player p2) {
		PlayerManager pm1 = PlayerManager.get(p1);
		
		pm1.removeRequest();
		
		p1.setGameMode(GameMode.SURVIVAL);
		p2.setGameMode(GameMode.SURVIVAL);
		
		p1.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + p2.getName());
		p2.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + p1.getName());
		
		teleportRandomArena(p1, p2);
	}
	
	public void endDuel(Player p) {
		PlayerManager pm = PlayerManager.get(p);
		
		InvView.getInstance().saveInv(p);
		
		pm.setStatus(PlayerStatus.SPAWN);
		pm.setOpponent(null);
		pm.setCanbuild(false);
		pm.showAllPlayer();
		
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		
		p.extinguish();
		p.clearPotionEffect();
		
		EnderDelay.getInstance().removeCooldown(p);
		
		if (!p.isDead()) {
			p.teleport(spawnLocation);
			p.setHealth(20.0D);
			p.setFoodLevel(20);
			p.setSaturation(10000f);
		}
	}
	
	public void sendWaitingMessage(final Player p1, final Player p2) {
		final Map<Player, Integer> cooldown = new HashMap<Player, Integer>();
		
		cooldown.put(p1, 5);
		
		new BukkitRunnable() {
			int num = cooldown.get(p1);
			
			@Override
			public void run() {
				if (p1 == null || p2 == null) {
					cooldown.remove(p1);
					this.cancel();
				}
				if(p1.isDead() || p2.isDead()) {
					cooldown.remove(p1);
					this.cancel();
				}
				if (PlayerManager.get(p1).getStatus() != PlayerStatus.WAITING || PlayerManager.get(p2).getStatus() != PlayerStatus.WAITING) {
					cooldown.remove(p1);
					this.cancel();
				}
				if (num <= 0) {
					p1.sendMessage(ChatColor.GREEN + "Duel has stated!");
					p2.sendMessage(ChatColor.GREEN + "Duel has stated!");
					p1.playSound(p1.getLocation(), Sound.FIREWORK_BLAST, 1.0f, 1.0f);
					p2.playSound(p2.getLocation(), Sound.FIREWORK_BLAST, 1.0f, 1.0f);
					cooldown.remove(p1);
					PlayerManager.get(p1).setStatus(PlayerStatus.DUEL);
					PlayerManager.get(p2).setStatus(PlayerStatus.DUEL);
					p1.showPlayer(p2);
					p2.showPlayer(p1);
					this.cancel();
				}
				if (num > 0) {
					p1.sendMessage(ChatColor.DARK_AQUA + "Duel start in " + ChatColor.YELLOW + num + ChatColor.DARK_AQUA + " second" + (num > 1 ? "s.." : ".."));
					p2.sendMessage(ChatColor.DARK_AQUA + "Duel start in " + ChatColor.YELLOW + num + ChatColor.DARK_AQUA + " second" + (num > 1 ? "s.." : ".."));
					p1.playSound(p1.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
					p2.playSound(p2.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
					cooldown.put(p1, num--);
				}
			}
		}.runTaskTimer(this, 20L, 20L);
	}
	
	private void teleportRandomArena(Player p1, Player p2) {
		PlayerManager pm1 = PlayerManager.get(p1);
		PlayerManager pm2 = PlayerManager.get(p2);
		
		pm1.setCanbuild(false);
		pm2.setCanbuild(false);
		
		pm1.setStatus(PlayerStatus.WAITING);
		pm2.setStatus(PlayerStatus.WAITING);
		
		pm1.setOpponent(p2);
		pm2.setOpponent(p1);
		
		p1.setHealth(20.0D);
		p2.setHealth(20.0D);
		
		p1.clearPotionEffect();
		p2.clearPotionEffect();
		
		p1.setFoodLevel(20);
		p1.setSaturation(20f);
		
		p2.setFoodLevel(20);
		p2.setSaturation(20f);
		
		p1.setNoDamageTicks(40);
		p2.setNoDamageTicks(40);
		
		pm1.hideAllPlayer();
		pm2.hideAllPlayer();
		
		Random random = new Random();
		int pickedArena = random.nextInt(arenaList.size()) + 1;
		
		p1.teleport(arenaList.get(pickedArena)[0]);
		p2.teleport(arenaList.get(pickedArena)[1]);
		
		p1.setSneaking(false);
		p2.setSneaking(false);
		
		pm1.giveKit();
		pm2.giveKit();
		
		sendWaitingMessage(p1, p2);
	}
}
