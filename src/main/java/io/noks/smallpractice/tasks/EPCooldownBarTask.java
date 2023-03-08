package io.noks.smallpractice.tasks;

import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import io.noks.smallpractice.Main;

public class EPCooldownBarTask extends BukkitRunnable {

	// TODO: EXECUTE IF THERE'S AT LEAST 1 DUEL THAT IS NOT BOXING IF THERE'S NOT CANCEL THE TASK AND RESTART IT WHEN A MATCH IS ACTUALLY GOING
	
	// SET TIMERTASK AND EXECUTE IT EVERY 1 tick
	
	@Override
	public void run() {
		// TODO
		// FOR THE DUELMANAGER uuidIdentifierToDuel FOR FASTER RESULT
		if (Main.getInstance().getDuelManager().getUniqueIDIdentifierToDuelMap().isEmpty()) {
			return;
		}
		for (UUID uuids : Main.getInstance().getDuelManager().getUniqueIDIdentifierToDuelMap().keySet()) {
			
		}
	}
}
