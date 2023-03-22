package io.noks.smallpractice.tasks;

import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.objects.duel.Duel;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class EPCooldownBarTask extends BukkitRunnable {

	// SET TIMERTASK AND EXECUTE IT EVERY 1 tick
	private boolean activated;
	private final Main main;
	public EPCooldownBarTask(Main main) {
		this.main = main;
		this.activated = false;
	}
	
	@Override
	public void run() {
		if (this.main.getDuelManager().getUniqueIDIdentifierToDuelMap().isEmpty()) {
			this.activated = false;
		}
		if (!this.activated) {
			this.cancel();
			return;
		}
		for (Map.Entry<UUID, Duel> entry : this.main.getDuelManager().getUniqueIDIdentifierToDuelMap().entrySet()) {
			final Duel duel = entry.getValue();
			if (duel.getLadder() != Ladders.NODEBUFF && duel.getLadder() != Ladders.NOENCHANT) {
				continue;
			}
			final PlayerManager pm = PlayerManager.get(entry.getKey());
			
			if (pm.getMatchStats().isEnderPearlCooldownActive()) {
				this.updateXpBar(pm);
				continue;
			}
			if (pm.getPlayer().getExp() != 0.0f) {
				pm.getPlayer().setExp(0.0f);
			}
		}
	}
	
	public void activate() {
		this.activated = true;
	}
	public boolean isActivated() {
		return this.activated;
	}
	
	private void updateXpBar(PlayerManager pm) { // TODO: IT DOESNT GO DOWN
		final Player player = pm.getPlayer();
	    final float xpPercentage = Math.max(0.0f, Math.min(99.9f, 100.0f - ((float) pm.getMatchStats().getEnderPearlCooldown() / (14 * 1000)) * 100));
	    player.setExp(xpPercentage / 100);
	}
}
