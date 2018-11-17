package us.noks.smallpractice.listeners;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.PlayerManager;
import us.noks.smallpractice.utils.InvView;
import us.noks.smallpractice.utils.PlayerStatus;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		event.setJoinMessage(null);
		
		player.setExp(0.0F);
		player.setLevel(0);
		
		player.setHealth(20.0D);
		player.setFoodLevel(20);
		player.setSaturation(1000f);
		player.extinguish();
		player.clearPotionEffect();
		player.setAllowFlight(false);
		player.setFlying(false);
		
		PlayerManager.get(player).setStatus(PlayerStatus.SPAWN);
		
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		
		player.setGameMode(GameMode.SURVIVAL);
		
		player.teleport(Main.getInstance().spawnLocation);
		
		player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
		player.sendMessage(ChatColor.YELLOW + "Welcome on the practice 1.0 of " + ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Halka");
		player.sendMessage("   ");
		player.sendMessage(ChatColor.YELLOW + "Our Twitter -> " + ChatColor.DARK_AQUA + "https://twitter.com/HalkaNetwork");
		player.sendMessage(ChatColor.YELLOW + "Our Discord -> " + ChatColor.DARK_AQUA + "https://discord.me/Halka");
		player.sendMessage(ChatColor.RED + "-> Keep in mind this is a beta ^^");
		player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
		player.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + "NEWS -> " + ChatColor.DARK_AQUA + "Fix all bugs for the beta ^^");
		
		player.setPlayerListName((player.isOp() ? ChatColor.DARK_AQUA : ChatColor.YELLOW) + player.getName());
		
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			PlayerManager pm = PlayerManager.get(allPlayers);
			if (pm.getStatus() == PlayerStatus.WAITING || pm.getStatus() == PlayerStatus.DUEL) {
				allPlayers.hidePlayer(player);
			}
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		event.setFormat((event.getPlayer().isOp() ? ChatColor.DARK_AQUA : ChatColor.YELLOW) + "%1$s" + ChatColor.RESET + ": %2$s");
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
		event.getDrops().clear();
		event.setDroppedExp(0);
		
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			Player killer = PlayerManager.get(player).getOpponent();
			
			if (killer != null) {
				InvView.getInstance().deathMsg(killer, player);
				
				Main.getInstance().endDuel(player);
				Main.getInstance().endDuel(killer);
			}
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		new BukkitRunnable() {
			
			@Override
			public void run() {
				p.teleport(Main.getInstance().spawnLocation);
			}
		}.runTaskLater(Main.getInstance(), 1L);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);
		
		if (Main.getInstance().queue.contains(event.getPlayer())) {
			Main.getInstance().queue.remove(event.getPlayer());
		}
		if (PlayerManager.get(event.getPlayer()).getStatus() == PlayerStatus.DUEL && PlayerManager.get(event.getPlayer()).getOpponent() != null) {
			Player op = PlayerManager.get(event.getPlayer()).getOpponent();
			
			InvView.getInstance().deathMsg(op, event.getPlayer());
			InvView.getInstance().saveInv(event.getPlayer());
			Main.getInstance().endDuel(op);
		}
		PlayerManager.remove(event.getPlayer());
	}
	
	@EventHandler
	public void onVoidDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			PlayerManager pm = PlayerManager.get(player);
			
			if (event.getCause() == DamageCause.FALL && (pm.getStatus() == PlayerStatus.SPAWN || pm.getStatus() == PlayerStatus.QUEUE)) {
				event.setCancelled(true);
			}
			if (event.getCause() == DamageCause.VOID && (pm.getStatus() == PlayerStatus.SPAWN || pm.getStatus() == PlayerStatus.QUEUE)) {
				event.setCancelled(true);
				player.teleport(Main.getInstance().spawnLocation);
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player rec = (Player) event.getEntity();
			Player attacker = (Player) event.getDamager();
				
			if (PlayerManager.get(attacker).getStatus() == PlayerStatus.SPECTATE) {
				event.setCancelled(true);
				return;
			}
			if (PlayerManager.get(attacker).getStatus() != PlayerStatus.DUEL && PlayerManager.get(rec).getStatus() != PlayerStatus.DUEL) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onReceiveDrop(PlayerPickupItemEvent event) {
		if (event.getItem().getOwner() instanceof Player) {
			Player owner = (Player) event.getItem().getOwner();
			
			if (!event.getPlayer().canSee(owner)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && PlayerManager.get(event.getPlayer()).getStatus() != PlayerStatus.WAITING && PlayerManager.get(event.getPlayer()).getStatus() != PlayerStatus.DUEL) {
			event.setCancelled(true);
		}
		if (event.getItemDrop().getItemStack().getType() == Material.GLASS_BOTTLE) event.getItemDrop().remove();
	}
	
	@EventHandler
	public void onFeed(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			
			if (PlayerManager.get(player).getStatus() == PlayerStatus.SPAWN || PlayerManager.get(player).getStatus() == PlayerStatus.QUEUE) {
				event.setCancelled(true);
			}
		}
	}
}
