package us.noks.smallpractice.objects.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.minecraft.util.com.google.common.collect.Maps;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.listeners.EnderDelay;
import us.noks.smallpractice.objects.Duel;
import us.noks.smallpractice.utils.InvView;
import us.noks.smallpractice.utils.PlayerStatus;

public class DuelManager {
	
	public static DuelManager instance = new DuelManager();
	public static DuelManager getInstance() {
		return instance;
	}
	
	private Map<Player, Duel> playerIdentifierToDuel = Maps.newHashMap();
	public Duel getDuelByPlayer(Player player) {
        return this.playerIdentifierToDuel.get(player);
    }
	
	public void startDuel(Player p1, Player p2) {
		PlayerManager pm1 = PlayerManager.get(p1);
		
		pm1.removeRequest();
		
		p1.setGameMode(GameMode.SURVIVAL);
		p2.setGameMode(GameMode.SURVIVAL);
		
		p1.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + p2.getName());
		p2.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + p1.getName());
		
		Scoreboard firstPlayerScoreboard = Main.getInstance().getServer().getScoreboardManager().getNewScoreboard();
		Team red1 = firstPlayerScoreboard.registerNewTeam("red");
		red1.setPrefix(ChatColor.RED.toString());
		Team green1 = firstPlayerScoreboard.registerNewTeam("green");
		green1.setPrefix(ChatColor.GREEN.toString());
        
		Scoreboard secondPlayerScoreboard = Main.getInstance().getServer().getScoreboardManager().getNewScoreboard();
		Team red2 = secondPlayerScoreboard.registerNewTeam("red");
		red2.setPrefix(ChatColor.RED.toString());
		Team green2 = secondPlayerScoreboard.registerNewTeam("green");
		green2.setPrefix(ChatColor.GREEN.toString());
        
		green1.addEntry(p1.getName());
		red2.addEntry(p1.getName());
        
		green2.addEntry(p2.getName());
		red1.addEntry(p2.getName());
        
		p1.setScoreboard(firstPlayerScoreboard);
		p2.setScoreboard(secondPlayerScoreboard);
		
		teleportRandomArena(new Duel(p1, p2));
	}
	
	public void endDuel(Duel duel) {
		Player p1 = duel.getFirstPlayer();
		Player p2 = duel.getSecondPlayer();
		
		PlayerManager pm1 = PlayerManager.get(p1);
		PlayerManager pm2 = PlayerManager.get(p2);
		
		pm1.setOldOpponent(p2);
		pm2.setOldOpponent(p1);
		
		p1.setScoreboard(Main.getInstance().getServer().getScoreboardManager().getNewScoreboard());
		p2.setScoreboard(Main.getInstance().getServer().getScoreboardManager().getNewScoreboard());
		
		InvView.getInstance().saveInv(p1);
		InvView.getInstance().saveInv(p2);
		
		pm1.setStatus(PlayerStatus.SPAWN);
		pm2.setStatus(PlayerStatus.SPAWN);
		
		pm1.setOpponent(null);
		pm2.setOpponent(null);
		
		pm1.setCanBuild(false);
		pm2.setCanBuild(false);
		
		pm1.showAllPlayer();
		pm2.showAllPlayer();
		
		p1.extinguish();
		p2.extinguish();
		
		p1.clearPotionEffect();
		p2.clearPotionEffect();
		
		EnderDelay.getInstance().removeCooldown(p1);
		EnderDelay.getInstance().removeCooldown(p2);
		
		Iterator<Player> it = duel.getAllSpectators().iterator();
		while (it.hasNext()) {
			Player spec = it.next();
			PlayerManager sm = PlayerManager.get(spec);
			
			spec.setAllowFlight(false);
			spec.setFlying(false);
			sm.setStatus(PlayerStatus.SPAWN);
			sm.showAllPlayer();
			sm.setSpectate(null);
			spec.teleport(Main.getInstance().spawnLocation);
			sm.giveSpawnItem();
			
			it.remove();
		}
		
		if (!p1.isDead() && p1.isOnline()) {
			p1.setHealth(20.0D);
			p1.setFoodLevel(20);
			p1.setSaturation(10000f);
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if (p1 != null) {
						p1.teleport(Main.getInstance().spawnLocation);
						pm1.giveSpawnItem();
					}
				}
			}.runTaskLater(Main.getInstance(), 40L);
		}
		if (!p2.isDead() && p2.isOnline()) {
			p2.setHealth(20.0D);
			p2.setFoodLevel(20);
			p2.setSaturation(10000f);
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if (p2 != null) {
						p2.teleport(Main.getInstance().spawnLocation);
						pm2.giveSpawnItem();
					}
				}
			}.runTaskLater(Main.getInstance(), 40L);
		}
		this.playerIdentifierToDuel.remove(p1);
		this.playerIdentifierToDuel.remove(p2);
	}
	
	public void sendWaitingMessage(Duel duel) {
		Map<Duel, Integer> cooldown = new HashMap<Duel, Integer>();
		Player p1 = duel.getFirstPlayer();
		Player p2 = duel.getSecondPlayer();
		
		cooldown.put(duel, 5);
		
		new BukkitRunnable() {
			int num = cooldown.get(duel);
			
			@Override
			public void run() {
				if (!cooldown.containsKey(duel)) {
					this.cancel();
				}
				if (p1 == null || p2 == null) {
					cooldown.remove(duel);
					this.cancel();
				}
				if(p1.isDead() || p2.isDead()) {
					cooldown.remove(duel);
					this.cancel();
				}
				if (PlayerManager.get(p1).getStatus() != PlayerStatus.WAITING || PlayerManager.get(p2).getStatus() != PlayerStatus.WAITING) {
					cooldown.remove(duel);
					this.cancel();
				}
				if (num <= 0) {
					p1.sendMessage(ChatColor.GREEN + "Duel has stated!");
					p2.sendMessage(ChatColor.GREEN + "Duel has stated!");
					p1.playSound(p1.getLocation(), Sound.FIREWORK_BLAST, 1.0f, 1.0f);
					p2.playSound(p2.getLocation(), Sound.FIREWORK_BLAST, 1.0f, 1.0f);
					PlayerManager.get(p1).setStatus(PlayerStatus.DUEL);
					PlayerManager.get(p2).setStatus(PlayerStatus.DUEL);
					p1.showPlayer(p2);
					p2.showPlayer(p1);
					cooldown.remove(duel);
					this.cancel();
				}
				if (num > 0) {
					p1.sendMessage(ChatColor.DARK_AQUA + "Duel start in " + ChatColor.YELLOW + num + ChatColor.DARK_AQUA + " second" + (num > 1 ? "s.." : ".."));
					p2.sendMessage(ChatColor.DARK_AQUA + "Duel start in " + ChatColor.YELLOW + num + ChatColor.DARK_AQUA + " second" + (num > 1 ? "s.." : ".."));
					p1.playSound(p1.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
					p2.playSound(p2.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
					cooldown.put(duel, num--);
				}
			}
		}.runTaskTimer(Main.getInstance(), 20L, 20L);
	}
	
	private void teleportRandomArena(Duel duel) {
		Player p1 = duel.getFirstPlayer();
		Player p2 = duel.getSecondPlayer();
		
		this.playerIdentifierToDuel.put(p1, duel);
		this.playerIdentifierToDuel.put(p2, duel);
		
		PlayerManager pm1 = PlayerManager.get(p1);
		PlayerManager pm2 = PlayerManager.get(p2);
		
		pm1.setCanBuild(false);
		pm2.setCanBuild(false);
		
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
		
		p1.setNoDamageTicks(50);
		p2.setNoDamageTicks(50);
		
		pm1.hideAllPlayer();
		pm2.hideAllPlayer();
		
		Random random = new Random();
		int pickedArena = random.nextInt(Main.getInstance().arenaList.size()) + 1;
		
		p1.teleport(Main.getInstance().arenaList.get(pickedArena)[0]);
		p2.teleport(Main.getInstance().arenaList.get(pickedArena)[1]);
		
		p1.setSneaking(false);
		p2.setSneaking(false);
		
		pm1.giveKit();
		pm2.giveKit();
		
		sendWaitingMessage(duel);
	}
}
