package io.noks.smallpractice.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.Ladders;
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
		
		if (!pm.isAllowedToBuild()) {
			event.setCancelled(true);
		}
		if (pm.getStatus() == PlayerStatus.DUEL) {
			final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(playerUUID);
			
			if (duel != null && duel.getBlockStorage() != null && duel.getLadder() == Ladders.BUILDUHC) {
				final Block block = event.getBlock();
				final Block placedBlock = event.getBlockPlaced();
				final BlockStorage storage = duel.getBlockStorage();
				storage.add(placedBlock, block);
					
				for (UUID uuids : duel.getAllAliveTeamsAndSpectators()) {
					final Player duelPlayers = this.main.getServer().getPlayer(uuids);
					if (duelPlayers == null) continue;
					block.getChunk().createFakeBlockUpdate(storage.getAllLocations(), storage.getAllIds(), storage.getAllDatas()).sendTo(duelPlayers);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		final UUID playerUUID = event.getPlayer().getUniqueId();
		final PlayerManager pm = PlayerManager.get(playerUUID);
		
		if (!pm.isAllowedToBuild()) {
			event.setCancelled(true);
		}
		if (pm.getStatus() == PlayerStatus.DUEL) {
			final Block block = event.getBlock();
			final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(playerUUID);
				
			if (duel != null && duel.getBlockStorage() != null) {
				final BlockStorage storage = duel.getBlockStorage();
				if (storage.contains(block.getLocation()) && duel.getLadder() == Ladders.BUILDUHC || block.getType() == Material.SNOW_BLOCK && duel.getLadder() == Ladders.SPLEEF) {
					storage.addAir(block.getLocation(), block);
				}
				for (UUID uuids : duel.getAllAliveTeamsAndSpectators()) {
					final Player duelPlayers = this.main.getServer().getPlayer(uuids);
					if (duelPlayers == null) continue;
					block.getChunk().createFakeBlockUpdate(storage.getAllLocations(), storage.getAllIds(), storage.getAllDatas()).sendTo(duelPlayers);
				}
			}
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
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBlockChangeByEntity(EntityChangeBlockEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
			return;
		}
		event.setCancelled(true);
	}
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBlockSpread(BlockSpreadEvent event) {
		if (event.getSource().getType() != Material.VINE) {
			return;
		}
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInteractWithItemFrame(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
			final Player interactor = event.getPlayer();
			if (PlayerManager.get(interactor.getUniqueId()).isAllowedToBuild()) {
				return;
			}
			event.setCancelled(true);
		}
	}
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDamageItemFrame(EntityDamageByEntityEvent  event) {
		if (event.getEntityType() == EntityType.ITEM_FRAME && event.getDamager() instanceof Player) {
			final Player interactor = (Player) event.getDamager();
			if (PlayerManager.get(interactor.getUniqueId()).isAllowedToBuild()) {
				return;
			}
			event.setCancelled(true);
		}
	}
}
