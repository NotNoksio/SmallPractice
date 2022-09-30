package io.noks.smallpractice.listeners;

import java.util.UUID;

import javax.swing.text.html.BlockView;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.Duel;
import io.noks.smallpractice.objects.managers.PlayerManager;
import net.md_5.bungee.api.ChatColor;

public class ServerListeners implements Listener {
	private Main main;
	public ServerListeners(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent event) {
		final Player player = event.getPlayer();
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		
		final Block blockPlaced = event.getBlockPlaced();
		if (pm.getStatus() == PlayerStatus.BRIDGE && blockPlaced.getType() != Material.TNT) {
			if (blockPlaced.getLocation().getY() >= 250) {
				event.setCancelled(true);
				return;
			}
			for (int i = 0; i < 8; i++) {
				if (event.getBlock().getLocation().subtract(0.0D, i, 0.0D).getBlock().getType() == Material.OBSIDIAN || event.getBlock().getLocation().subtract(0.0D, i, 0.0D).getBlock().getType() == Material.GLOWSTONE) {
					event.setCancelled(true);
				}
			}
			return;
		}
		if (!pm.isAllowedToBuild()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		final Player player = event.getPlayer();
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		final Block block = event.getBlock();
		
		if (pm.getStatus() == PlayerStatus.BRIDGE && block.getType() != Material.SANDSTONE) {
			event.setCancelled(true);
			return;
		}
		// SPLEEF TEST
		if(pm.getStatus() == PlayerStatus.DUEL && block.getType() == Material.SNOW_BLOCK) {
			final Duel currentDuel = this.main.getDuelManager().getDuelFromPlayerUUID(player.getUniqueId());
			if (currentDuel == null) {
				return;
			}
			if (currentDuel.getLadder() == Ladders.SPLEEF) {
				event.setCancelled(true);
				for (UUID uuid : currentDuel.getAllAliveTeamsAndSpectators()) {
					final Player blockViewers = Bukkit.getPlayer(uuid);
					blockViewers.sendBlockChange(block.getLocation(), Material.AIR.getId(), (byte)0);
				}
				currentDuel.addBrokenBlocksLocation(block.getLocation());
			}
			return;
		}
		// SPLEEF TEST
		if (!pm.isAllowedToBuild()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST) 
	public void onBucketFill(PlayerBucketFillEvent event) {
		final Player player = event.getPlayer();
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		
		if (!pm.isAllowedToBuild()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		final Player player = event.getPlayer();
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		
		if (!pm.isAllowedToBuild()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onWeather(WeatherChangeEvent event) {
		if (event.toWeatherState()) event.setCancelled(true);
	}
	
	@EventHandler
	public void onMotd(ServerListPingEvent event) {
		event.setMotd(this.getMotd());
	}
	
	private final String getMotd() {
		final String line1 = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Bawz " + ChatColor.GRAY + "(Practice "  + main.getDescription().getVersion() + ")\n";
		final String line2 = ChatColor.YELLOW + "US Proxy pot pvp server" + (Bukkit.getServer().hasWhitelist() ? ChatColor.RED + " Whitelisted..." : "");
		return (line1 + line2);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onTnt(EntityExplodeEvent event) {
		if (event.blockList().isEmpty()) {
			return;
		}
		event.blockList().clear();
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPrepareCraft(PrepareItemCraftEvent event) {
		if (event.getInventory().getResult().getType() != Material.MUSHROOM_SOUP) {
			event.getInventory().setResult(null);
		}
	}
}
