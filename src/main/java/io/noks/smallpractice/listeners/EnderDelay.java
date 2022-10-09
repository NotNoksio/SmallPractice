package io.noks.smallpractice.listeners;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.MatchStats;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class EnderDelay implements Listener {	
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.hasItem()) {
			return;
		}
		final ItemStack item = event.getItem();
		final Player player = event.getPlayer();
		if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && item.getType() == Material.ENDER_PEARL && player.getGameMode() != GameMode.CREATIVE) {
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			if (pm.getStatus() != PlayerStatus.DUEL) {
				event.setUseItemInHand(Result.DENY);
				player.sendMessage(ChatColor.RED + "You cannot use enderpearl here!");
				player.updateInventory();
				return;
			}
			final MatchStats matchStats = pm.getMatchStats();
			if (!matchStats.isEnderPearlCooldownActive()) {
				matchStats.applyEnderPearlCooldown();
				return;
			}
			event.setUseItemInHand(Result.DENY);
			final double time = matchStats.getEnderPearlCooldown() / 1000.0D;
			final DecimalFormat df = new DecimalFormat("#.#");
			player.sendMessage(ChatColor.DARK_AQUA + "Pearl cooldown: " + ChatColor.YELLOW + df.format(time) + " second" + (time > 1.0D ? "s" : ""));
			player.updateInventory();
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onTeleport(PlayerTeleportEvent event) {
		if (event.getCause() == TeleportCause.ENDER_PEARL) {
			final Player player = event.getPlayer();
			
			if (PlayerManager.get(player.getUniqueId()).getStatus() != PlayerStatus.DUEL) event.setCancelled(true);
		}
	}
}
