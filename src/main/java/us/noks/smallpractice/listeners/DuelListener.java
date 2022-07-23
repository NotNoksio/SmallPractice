package us.noks.smallpractice.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.enums.RemoveReason;
import us.noks.smallpractice.objects.Duel;
import us.noks.smallpractice.objects.MatchStats;
import us.noks.smallpractice.objects.managers.PlayerManager;

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
			final PlayerManager sm = new PlayerManager().get(shooter.getUniqueId());
			
			if ((sm.getStatus() == PlayerStatus.DUEL || sm.getStatus() == PlayerStatus.WAITING) && !event.getAffectedEntities().contains(shooter)) {
				final MatchStats stats = sm.getMatchStats();
				int cacheFailedPotions = stats.getFailedPotions() + 1;
				stats.setFailedPotions(cacheFailedPotions);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.isCancelled()) {
			return;
		}
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            final PlayerManager dm = new PlayerManager().get(e.getEntity().getUniqueId());
            final PlayerManager am = new PlayerManager().get(e.getDamager().getUniqueId());
            
            if(am.getStatus() == PlayerStatus.DUEL && dm.getStatus() == PlayerStatus.DUEL) { // TODO: Allow different target
            	final MatchStats damagedStats = dm.getMatchStats();
            	final MatchStats attackerStats = am.getMatchStats();
            	attackerStats.setHit(attackerStats.getHit() + 1);
            	attackerStats.setCombo(attackerStats.getCombo() + 1);
            	if(damagedStats.getCombo() > damagedStats.getLongestCombo()) {
            		damagedStats.setLongestCombo(damagedStats.getCombo());
            	}
            	damagedStats.setCombo(0);
            	Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(am.getPlayerUUID());
            	if (duel.getLadder() == Ladders.BOXING) {
            		am.getPlayer().setLevel(attackerStats.getHit());
            		if (attackerStats.getHit() == 100) {
            			this.main.getDuelManager().removePlayerFromDuel(dm.getPlayer(), RemoveReason.KILLED);
            		}
            	}
            }
        }
    }
	
	// My PlayerMoveEvent is not like everyone event (be careful)
	@EventHandler(priority=EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent event) {
		final Player player = event.getPlayer();
		final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(player.getUniqueId());
		if (duel != null && duel.getLadder() == Ladders.SUMO) {
			final Arenas arena = duel.getArena();
			if (player.getLocation().getBlockY() < arena.getMiddle().getBlockY() || player.getLocation().distance(arena.getMiddle()) > 10 || player.getLocation().getBlock().getType() == Material.WATER) { // Put multiple end check
				this.main.getDuelManager().removePlayerFromDuel(player, RemoveReason.KILLED);
			}
		}
	}
}
