package us.noks.smallpractice.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.github.paperspigot.event.entity.ProjectileCollideEvent;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.enums.PlayerStatus;
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
			for (LivingEntity affectedEntities : event.getAffectedEntities()) {
				Player affectedPlayers = (Player) affectedEntities;
				if (!shooter.canSee(affectedPlayers)) {
					event.setCancelled(true);
				}
			}
			if (event.isCancelled()) return;
			final PlayerManager sm = PlayerManager.get(shooter.getUniqueId());
			
			if ((sm.getStatus() == PlayerStatus.DUEL || sm.getStatus() == PlayerStatus.WAITING) && !event.getAffectedEntities().contains(shooter)) {
				final MatchStats stats = sm.getMatchStats();
				byte cacheFailedPotions = stats.getFailedPotions();
				stats.setFailedPotions(cacheFailedPotions++);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.isCancelled()) {
			return;
		}
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            final PlayerManager dm = PlayerManager.get(e.getEntity().getUniqueId());
            final PlayerManager am = PlayerManager.get(e.getDamager().getUniqueId());
            
            if(am.getStatus() == PlayerStatus.DUEL && dm.getStatus() == PlayerStatus.DUEL) {
            	final MatchStats damagedStats = dm.getMatchStats();
            	final MatchStats attackerStats = am.getMatchStats();
            	attackerStats.setHit(attackerStats.getHit() + 1);
            	attackerStats.setCombo(attackerStats.getCombo() + 1);
            	if(damagedStats.getCombo() > damagedStats.getLongestCombo()) {
            		damagedStats.setLongestCombo(damagedStats.getCombo());
            	}
            	damagedStats.setCombo(0);
            }
        }
    }
	
	@EventHandler
	public void onProjectileColide(ProjectileCollideEvent event) {
		if (event.getEntity().getShooter() instanceof Player && event.getCollidedWith() instanceof Player) {
			final Player shooter = (Player) event.getEntity().getShooter();
			final Player collide = (Player) event.getCollidedWith();
			
			if (!shooter.canSee(collide)) event.setCancelled(true);
		}
	}
}
