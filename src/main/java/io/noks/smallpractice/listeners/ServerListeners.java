package io.noks.smallpractice.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.duel.Duel;
import io.noks.smallpractice.objects.managers.PlayerManager;
import io.noks.smallpractice.utils.BlockStorage;
import net.md_5.bungee.api.ChatColor;

public class ServerListeners implements Listener {
	private Main main;
	public ServerListeners(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent event) {
		final UUID playerUUID = event.getPlayer().getUniqueId();
		final PlayerManager pm = PlayerManager.get(playerUUID);
		
		// TODO: register blocks in duel if BUILDUHC
		if (!pm.isAllowedToBuild()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		final UUID playerUUID = event.getPlayer().getUniqueId();
		final PlayerManager pm = PlayerManager.get(playerUUID);
		
		if (pm.getStatus() == PlayerStatus.DUEL) {
			final Block block = event.getBlock();
			if (block.getType() == Material.SNOW_BLOCK) {
				final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(playerUUID);
				
				if (duel != null && duel.getBlockStorage() != null) {
					final BlockStorage storage = duel.getBlockStorage();
					storage.addAir(block.getLocation());
					
					for (UUID uuids : duel.getAllAliveTeamsAndSpectators()) {
						final Player duelPlayers = this.main.getServer().getPlayer(uuids);
						if (duelPlayers == null) continue;
						block.getChunk().createFakeBlockUpdate(storage.getAllLocations(), storage.getAllIds(), storage.getAllDatas()).sendTo(duelPlayers);
					}
				}
			}
		}
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
		final String line1 = ChatColor.translateAlternateColorCodes('&', this.main.getConfigManager().motdFirstLine) + "\n";
		final String line2 = ChatColor.translateAlternateColorCodes('&', this.main.getConfigManager().motdSecondLine) + (Bukkit.getServer().hasWhitelist() ? ChatColor.RED + " Whitelisted..." : "");
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
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBedEnter(PlayerBedEnterEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onLeavesDecay(LeavesDecayEvent event) {
		event.setCancelled(true);
	}
}
