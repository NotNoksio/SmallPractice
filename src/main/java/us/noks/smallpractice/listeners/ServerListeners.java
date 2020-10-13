package us.noks.smallpractice.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import net.md_5.bungee.api.ChatColor;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.PlayerManager;

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
		
		if (!pm.isAllowedToBuild()) {
			event.setCancelled(true);
			return;
		}
		if (pm.getStatus() == PlayerStatus.BRIDGE) {
			for (int i = 0; i < 8; i++) {
				if (event.getBlock().getLocation().subtract(0.0D, i, 0.0D).getBlock().getType() == Material.OBSIDIAN || event.getBlock().getLocation().subtract(0.0D, i, 0.0D).getBlock().getType() == Material.GLOWSTONE) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		final Player player = event.getPlayer();
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		
		if (!pm.isAllowedToBuild()) {
			event.setCancelled(true);
			return;
		}
		if (pm.getStatus() == PlayerStatus.BRIDGE) {
			if (event.getBlock().getType() != Material.SANDSTONE) {
				event.setCancelled(true);
			}
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
		final String line1 = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Goneko " + ChatColor.GRAY + "(Practice "  + main.getDescription().getVersion() + ")\n";
		final String line2 = ChatColor.YELLOW + "Pot pvp server" + (Bukkit.getServer().hasWhitelist() ? ChatColor.RED + " Whitelisted..." : "");
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
