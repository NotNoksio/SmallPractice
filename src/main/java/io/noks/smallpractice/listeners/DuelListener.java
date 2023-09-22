package io.noks.smallpractice.listeners;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.enums.RemoveReason;
import io.noks.smallpractice.objects.MatchStats;
import io.noks.smallpractice.objects.duel.Duel;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class DuelListener implements Listener {
	private Main main;
	public DuelListener(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onFailedPotion(PotionSplashEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getEntity().getShooter() instanceof Player) {
			final Player shooter = (Player) event.getEntity().getShooter();
			final PlayerManager sm = PlayerManager.get(shooter.getUniqueId());
			
			if ((sm.getStatus() == PlayerStatus.DUEL || sm.getStatus() == PlayerStatus.WAITING) && !event.getAffectedEntities().contains(shooter)) {
				final MatchStats stats = sm.getMatchStats();
				stats.addFailedPotions();
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            final PlayerManager dm = PlayerManager.get(event.getEntity().getUniqueId());
            if (dm.isFrozen()) {
            	return;
            }
            final PlayerManager am = PlayerManager.get(event.getDamager().getUniqueId());
            
            if(am.getStatus() == PlayerStatus.DUEL && dm.getStatus() == PlayerStatus.DUEL) {
            	final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(am.getPlayerUUID());
            	if (duel == null) {
            		return;
            	}
            	final MatchStats damagedStats = dm.getMatchStats();
            	if (duel.getLadder() != Ladders.COMBO) {
		            if (damagedStats.containsNextHitUUID(am.getPlayerUUID()) && damagedStats.getNextHitTick(am.getPlayerUUID()) > System.currentTimeMillis()) {
		            	return;
		            }
		            damagedStats.updateNextHitTick(am.getPlayerUUID());
            	}
            	final MatchStats attackerStats = am.getMatchStats();
            	attackerStats.addHit(dm.getPlayerUUID());
            	attackerStats.setCombo(attackerStats.getCombo() + 1);
            	
            	if(damagedStats.getCombo() > damagedStats.getLongestCombo()) {
            		damagedStats.setLongestCombo(damagedStats.getCombo());
            	}
            	damagedStats.setCombo(0);
            	if (duel.getLadder() == Ladders.SUMO) {
            		event.setDamage(0.0D);
            		return;
            	}
            	if (duel.getLadder() == Ladders.BOXING) {
            		final int hit = attackerStats.getHit(dm.getPlayerUUID());
            		am.getPlayer().setLevel(hit);
            		am.getPlayer().setExp(Math.min((hit / 100.0f), 99.9f));
            		if (hit == 100) {
            			// TODO: in teamfight we cant just set him 0 hit while if this its a 5v5 the 4 others have at least between 30 to 60
            			this.main.getDuelManager().removePlayerFromDuel(dm.getPlayer(), RemoveReason.KILLED);
            			am.getPlayer().setLevel(0);
            			am.getPlayer().setExp(0.0f);
            		}
            	}
            }
        }
    }
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onEntitySpawnInWorld(EntitySpawnEvent event) {
		if (event.getEntity() instanceof Item) {
			final Item itemDropped = (Item) event.getEntity();
			
			if (itemDropped.getItemStack().getType() == Material.GLASS_BOTTLE || itemDropped.getItemStack().getType() == Material.BOWL) return;
			if (itemDropped.getOwner() != null && itemDropped.getOwner() instanceof Player) {
				final UUID playerUUID = itemDropped.getOwner().getUniqueId();
				final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(playerUUID);
				if (duel == null) return;
				if (duel.containDrops(itemDropped.getUniqueId())) return;
				duel.addDrops(itemDropped.getUniqueId());
			}
			return;
		}
		if (event.getEntity() instanceof Arrow) {
			final Arrow arrow = (Arrow) event.getEntity();
			
			if (arrow.getShooter() != null && arrow.getShooter() instanceof Player) {
				final Player playerUUID = (Player) arrow.getShooter();
				final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(playerUUID.getUniqueId());
				if (duel == null) return;
				if (duel.containDrops(arrow.getUniqueId())) return;
				duel.addDrops(arrow.getUniqueId());
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityDespawnFromWorld(EntityDeathEvent event) {
		if (this.main.getDuelManager().getAllDuels().isEmpty()) {
			return;
		}
		if (event.getEntity() instanceof Item) {
			final Item item = (Item) event.getEntity();
			if (item.getOwner() == null || !(item.getOwner() instanceof Player)) return;
			for (Duel duels : this.main.getDuelManager().getAllDuels()) {
				if (duels == null || !duels.containDrops(item.getUniqueId())) continue;
				duels.removeDrops(item.getUniqueId());
			}
		}
		if (event.getEntity() instanceof Arrow) {
			final Arrow arrow = (Arrow) event.getEntity();
			if (arrow.getShooter() == null || !(arrow.getShooter() instanceof Player)) return;
			for (Duel duels : this.main.getDuelManager().getAllDuels()) {
				if (duels == null || !duels.containDrops(arrow.getUniqueId())) continue;
				duels.removeDrops(arrow.getUniqueId());
			}
		}
	}
	
	// My PlayerMoveEvent is not like everyone event (be careful)
	@EventHandler(priority=EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		if (PlayerManager.get(uuid).isFrozen()) {
			event.setCancelled(true);
			return;
		}
		if (this.main.getArenaManager().getArenaList(true, true).isEmpty()) { // Dont run event if we dont need it
			return;
		}
		if (this.needMoveEventTrigger()) { // Dont run event if we dont need it
			return;
		}
		final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(uuid);
		if (duel != null && (duel.getLadder() == Ladders.SUMO || duel.getLadder() == Ladders.SPLEEF)) {
			if (!duel.getAllAliveTeams().contains(uuid)) {
				return;
			}
			final Player player = event.getPlayer();
			if (PlayerManager.get(player.getUniqueId()).getStatus() == PlayerStatus.WAITING) {
				event.setCancelled(true);
				return;
			}
			final Arena arena = duel.getArena();
			if (arena.isSpleef() && player.getLocation().getBlock().isLiquid()) {
				this.main.getDuelManager().removePlayerFromDuel(player, RemoveReason.KILLED);
				return;
			}
			if (player.getLocation().getBlockY() < arena.getMiddle().getBlockY() || player.getLocation().distance(arena.getMiddle()) > 10 || player.getLocation().getBlock().isLiquid()) { // Put multiple end check
				this.main.getDuelManager().removePlayerFromDuel(player, RemoveReason.KILLED);
			}
		}
	}
	
	private boolean needMoveEventTrigger() {
		return !(this.main.getDuelManager().getFightFromLadder(Ladders.SUMO, false) == 0 && this.main.getDuelManager().getFightFromLadder(Ladders.SUMO, true) == 0 || this.main.getDuelManager().getFightFromLadder(Ladders.SPLEEF, false) == 0 && this.main.getDuelManager().getFightFromLadder(Ladders.SPLEEF, true) == 0);
	}
}
