package us.noks.smallpractice.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.utils.PlayerStatus;

public class DuelListener implements Listener {
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onFailedPotion(PotionSplashEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player shooter = (Player) event.getEntity().getShooter();
			PlayerManager sm = PlayerManager.get(shooter);
			
			if (sm.getStatus() == PlayerStatus.DUEL && !event.getAffectedEntities().contains(shooter)) {
				sm.setFailedPotions(sm.getFailedPotions() + 1);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player damaged = (Player)e.getEntity();
            Player attacker = (Player)e.getDamager();
            
            PlayerManager dm = PlayerManager.get(damaged);
            PlayerManager am = PlayerManager.get(attacker);
            
            if(am.getStatus() == PlayerStatus.DUEL && dm.getStatus() == PlayerStatus.DUEL) {
            	am.setHit(am.getHit() + 1);
            	am.setCombo(am.getCombo() + 1);
            	if(dm.getCombo() > dm.getLongestCombo()) {
            		dm.setLongestCombo(dm.getCombo());
            	}
            	dm.setCombo(0);
            }
        }
    }
}
