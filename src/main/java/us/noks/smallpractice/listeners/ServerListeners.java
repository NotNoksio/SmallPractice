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
import us.noks.smallpractice.objects.managers.PlayerManager;

public class ServerListeners implements Listener {
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		
		if (!PlayerManager.get(player).isCanBuild()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		
		if (!PlayerManager.get(player).isCanBuild()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onWeather(WeatherChangeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onMotd(ServerListPingEvent event) {
		String line1 = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Halka " + ChatColor.GRAY + "(Practice "  + Main.getInstance().getDescription().getVersion() + ")\n";
		String line2 = ChatColor.YELLOW + "Home of the pots pvp";
		event.setMotd(line1 + line2 + (Bukkit.getServer().hasWhitelist() ? ChatColor.RED + " Whitelisted..." : ""));
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onTnt(EntityExplodeEvent event) {
		event.blockList().clear();
	}
}
