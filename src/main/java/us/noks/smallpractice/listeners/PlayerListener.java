package us.noks.smallpractice.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.managers.DuelManager;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.utils.InvView;
import us.noks.smallpractice.utils.PlayerStatus;

public class PlayerListener implements Listener {
	
	@EventHandler(priority=EventPriority.HIGH)
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
		
		player.setGameMode(GameMode.SURVIVAL);
		
		player.setScoreboard(Main.getInstance().getServer().getScoreboardManager().getNewScoreboard());
		
		player.teleport(Main.getInstance().spawnLocation);
		PlayerManager.get(player).giveSpawnItem();
		
		player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
		player.sendMessage(ChatColor.YELLOW + "Welcome on the " + ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Halka" + ChatColor.YELLOW + " practice " + Main.getInstance().getDescription().getVersion() + " server");
		player.sendMessage("   ");
		player.sendMessage(ChatColor.YELLOW + "Noksio (Creator) Twitter -> " + ChatColor.DARK_AQUA + "https://twitter.com/NotNoksio");
		player.sendMessage(ChatColor.YELLOW + "Noksio (Creator) Discord -> " + ChatColor.DARK_AQUA + "https://discord.gg/TZhyPnB");
		player.sendMessage(ChatColor.RED + "-> Keep in mind this is a beta ^^");
		player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
		
		player.setPlayerListName(PlayerManager.get(player).getColorPrefix() + player.getName());
		
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			PlayerManager pmAll = PlayerManager.get(allPlayers);
			if (pmAll.getStatus() == PlayerStatus.WAITING || pmAll.getStatus() == PlayerStatus.DUEL) {
				allPlayers.hidePlayer(player);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
		event.getDrops().clear();
		event.setDroppedExp(0);
		
		if (event.getEntity() instanceof Player) {
			Player killed = event.getEntity();
			Player killer = PlayerManager.get(killed).getOpponent();
			
			if (killer != null) {
				InvView.getInstance().deathMsg(killer, killed);
				
				DuelManager.getInstance().endDuel(DuelManager.getInstance().getDuelByPlayer(killer));
				
				new BukkitRunnable() {
					
					@Override
					public void run() {
						if (killed.isDead() && killed != null) {
							killed.spigot().respawn();
						}
					}
				}.runTaskLater(Main.getInstance(), 50L);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		new BukkitRunnable() {
			
			@Override
			public void run() {
				p.setHealth(20.0D);
				p.setFoodLevel(20);
				p.setSaturation(10000f);
				p.teleport(Main.getInstance().spawnLocation);
				PlayerManager.get(p).giveSpawnItem();
			}
		}.runTaskLater(Main.getInstance(), 1L);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);
		
		if (Main.getInstance().queue.contains(event.getPlayer())) {
			Main.getInstance().queue.remove(event.getPlayer());
		}
		if ((PlayerManager.get(event.getPlayer()).getStatus() == PlayerStatus.DUEL || PlayerManager.get(event.getPlayer()).getStatus() == PlayerStatus.WAITING) && PlayerManager.get(event.getPlayer()).getOpponent() != null) {
			Player op = PlayerManager.get(event.getPlayer()).getOpponent();
			
			InvView.getInstance().deathMsg(op, event.getPlayer());
			/*
			PlayerManager.get(event.getPlayer()).setOldOpponent(op);
			InvView.getInstance().saveInv(event.getPlayer());
			
			Iterator<Player> it = PlayerManager.get(event.getPlayer()).getAllSpectators().iterator();
			
			while (it.hasNext()) {
				Player spec = it.next();
				PlayerManager sm = PlayerManager.get(spec);
				
				spec.setAllowFlight(false);
				spec.setFlying(false);
				sm.setStatus(PlayerStatus.SPAWN);
				sm.showAllPlayer();
				sm.setSpectate(null);
				spec.getInventory().clear();
				spec.teleport(Main.getInstance().spawnLocation);
				
				it.remove();
			}
			*/
			DuelManager.getInstance().endDuel(DuelManager.getInstance().getDuelByPlayer(op));
		}
		PlayerManager.remove(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
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
	
	@EventHandler(priority=EventPriority.LOWEST)
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
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onReceiveDrop(PlayerPickupItemEvent event) {
		if (event.getItem().getOwner() instanceof Player) {
			Player owner = (Player) event.getItem().getOwner();
			
			if (!event.getPlayer().canSee(owner)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDrop(PlayerDropItemEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && PlayerManager.get(event.getPlayer()).getStatus() != PlayerStatus.WAITING && PlayerManager.get(event.getPlayer()).getStatus() != PlayerStatus.DUEL) {
			event.setCancelled(true);
		}
		if (event.getItemDrop().getItemStack().getType() == Material.GLASS_BOTTLE) event.getItemDrop().remove();
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onClickItem(PlayerInteractEvent event) {
		Player p = event.getPlayer();
        ItemStack item = p.getItemInHand();
        if (p.getInventory().getItemInHand() == null || !p.getInventory().getItemInHand().hasItemMeta() || !p.getInventory().getItemInHand().getItemMeta().hasDisplayName()) {
            return;
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
        	switch (PlayerManager.get(p).getStatus()) {
			case SPAWN:
				if (item.getType() == Material.DIAMOND_SWORD && item.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Direct Queue")) {
	                event.setCancelled(true);
	                p.performCommand("random");
	            }
				break;
			case QUEUE:
				if (item.getType() == Material.REDSTONE && item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Leave Queue")) {
	                event.setCancelled(true);
	                p.performCommand("cancel");
	            }
				break;
			case SPECTATE:
				if (item.getType() == Material.REDSTONE && item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Leave Spectate")) {
	                event.setCancelled(true);
	                p.performCommand("leave");
	            }
				break;
			default:
				break;
			}
        }
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDrag(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		if (event.getInventory().getType().equals(InventoryType.CREATIVE) || event.getInventory().getType().equals(InventoryType.CRAFTING) || event.getInventory().getType().equals(InventoryType.PLAYER)) {
			if (PlayerManager.get(p).getStatus() != PlayerStatus.DUEL && PlayerManager.get(p).getStatus() != PlayerStatus.WAITING && !PlayerManager.get(p).isCanBuild()) {
				event.setCancelled(true);
				p.updateInventory();
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onFeed(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			
			if (PlayerManager.get(player).getStatus() == PlayerStatus.SPAWN || PlayerManager.get(player).getStatus() == PlayerStatus.QUEUE || PlayerManager.get(player).getStatus() == PlayerStatus.SPECTATE) {
				event.setCancelled(true);
			}
		}
	}
}
