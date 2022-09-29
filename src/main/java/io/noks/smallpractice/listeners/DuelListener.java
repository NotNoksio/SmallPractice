package io.noks.smallpractice.listeners;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena.Arenas;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.enums.RemoveReason;
import io.noks.smallpractice.objects.Duel;
import io.noks.smallpractice.objects.MatchStats;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class DuelListener implements Listener {
	private Main main;
	public DuelListener(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onFailedPotion(PotionSplashEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			final Player shooter = (Player) event.getEntity().getShooter();
			final PlayerManager sm = PlayerManager.get(shooter.getUniqueId());
			
			if ((sm.getStatus() == PlayerStatus.DUEL || sm.getStatus() == PlayerStatus.WAITING) && !event.getAffectedEntities().contains(shooter)) {
				final MatchStats stats = sm.getMatchStats();
				final int cacheFailedPotions = stats.getFailedPotions() + 1;
				stats.setFailedPotions(cacheFailedPotions);
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
            final PlayerManager am = PlayerManager.get(event.getDamager().getUniqueId());
            
            if(am.getStatus() == PlayerStatus.DUEL && dm.getStatus() == PlayerStatus.DUEL) {
            	final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(am.getPlayerUUID());
            	if (duel.getLadder() != Ladders.COMBO) {
            		final MatchStats damagedStats = dm.getMatchStats();
		            if (damagedStats.getNextHitTick() != 0 && damagedStats.getNextHitTick() > System.currentTimeMillis()) {
		            	return;
		            }
		            damagedStats.updateNextHitTick();
            	}
            	final MatchStats attackerStats = am.getMatchStats();
            	attackerStats.setHit(attackerStats.getHit() + 1);
            	attackerStats.setCombo(attackerStats.getCombo() + 1);
            	
            	final MatchStats damagedStats = dm.getMatchStats();
            	if(damagedStats.getCombo() > damagedStats.getLongestCombo()) {
            		damagedStats.setLongestCombo(damagedStats.getCombo());
            	}
            	damagedStats.setCombo(0);
            	if (duel.getLadder() == Ladders.SUMO) {
            		event.setDamage(0.0D);
            		return;
            	}
            	if (duel.getLadder() == Ladders.BOXING) {
            		final int hit = attackerStats.getHit();
            		am.getPlayer().setLevel(hit);
            		am.getPlayer().setExp((hit / 100.0f));
            		if (hit == 100) {
            			this.main.getDuelManager().endDuel(duel, (duel.getFirstTeam().contains(am.getPlayerUUID()) ? 1 : 2));
            		}
            	}
            }
        }
    }
	
	@EventHandler
	public void onEntitySpawnInWorld(EntitySpawnEvent event) {
		if (event.getEntity() instanceof Item) {
			final Item itemDropped = (Item) event.getEntity();
			
			if (itemDropped.getItemStack().getType() == Material.GLASS_BOTTLE || itemDropped.getItemStack().getType() == Material.BOWL) return;
			if (itemDropped.getOwner() != null && itemDropped.getOwner() instanceof Player) {
				UUID playerUUID = itemDropped.getOwner().getUniqueId();
				Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(playerUUID);
				if (duel == null) return;
				
				duel.addDrops(itemDropped);
			}
		}
	}
	
	// My PlayerMoveEvent is not like everyone event (be careful)
	@EventHandler(priority=EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent event) {
		final Player player = event.getPlayer();
		final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(player.getUniqueId());
		if (duel != null && duel.getLadder() == Ladders.SUMO) {
			if (!duel.getAllAliveTeams().contains(player.getUniqueId())) {
				return;
			}
			final Arenas arena = duel.getArena();
			if (player.getLocation().getBlockY() < arena.getMiddle().getBlockY() || player.getLocation().distance(arena.getMiddle()) > 10 || player.getLocation().getBlock().isLiquid()) { // Put multiple end check
				this.main.getDuelManager().removePlayerFromDuel(player, RemoveReason.KILLED);
			}
		}
	}
}
