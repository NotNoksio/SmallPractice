package us.noks.smallpractice.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.MatchStats;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class DuelListener implements Listener {
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onFailedPotion(PotionSplashEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			final Player shooter = (Player) event.getEntity().getShooter();
			final PlayerManager sm = PlayerManager.get(shooter.getUniqueId());
			
			if ((sm.getStatus() == PlayerStatus.DUEL || sm.getStatus() == PlayerStatus.WAITING) && !event.getAffectedEntities().contains(shooter)) {
				final MatchStats stats = sm.getMatchStats();
				int cacheFailedPotions = stats.getFailedPotions();
				stats.setFailedPotions(cacheFailedPotions++);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            final Player damaged = (Player)e.getEntity();
            final Player attacker = (Player)e.getDamager();
            
            final PlayerManager dm = PlayerManager.get(damaged.getUniqueId());
            final PlayerManager am = PlayerManager.get(attacker.getUniqueId());
            
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
}
