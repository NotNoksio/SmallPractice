package us.noks.smallpractice.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import net.md_5.bungee.api.ChatColor;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.PlayerManager;

public class ServerListeners implements Listener {
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		
		if (!PlayerManager.get(p).isCanbuild()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		
		if (!PlayerManager.get(p).isCanbuild()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onWeather(WeatherChangeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onMotd(ServerListPingEvent event) {
		event.setMotd(ChatColor.translateAlternateColorCodes('&', "&c&lVitaPot &7(Practice " + Main.getInstance().getDescription().getVersion() + ")") + "\n" + ChatColor.translateAlternateColorCodes('&', (!Bukkit.getServer().hasWhitelist() ? "&eHome of the pots pvp" : "&eHome of the pots pvp &cWhitelisted... ")));
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onTnt(EntityExplodeEvent event) {
		event.blockList().clear();
	}
}
