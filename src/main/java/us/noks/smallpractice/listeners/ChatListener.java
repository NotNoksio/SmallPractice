package us.noks.smallpractice.listeners;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class ChatListener implements Listener {
	private Main main;
	public ChatListener(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		final Player player = event.getPlayer();
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
		String prefix = pm.getColoredPrefix() + "%1$s" + pm.getColoredSuffix() + ChatColor.RESET;
		event.setFormat(prefix + ChatColor.WHITE + ": %2$s");
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerGetMentioned(AsyncPlayerChatEvent event) {
		final String message = event.getMessage();
		Iterator<Player> iterator = event.getRecipients().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (!message.matches(".*\\b(?i)" + player.getName() + "\\b.*")) {
				continue;
			}
			if (player.getName() == event.getPlayer().getName()) {
				continue;
			}
			String mentionMessage = event.getMessage().replaceAll("\\b(?i)" + player.getName() + "\\b", ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + player.getName() + ChatColor.RESET);
			player.sendMessage(String.format(event.getFormat(), event.getPlayer().getName(), mentionMessage));
			iterator.remove();
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onStaffChat(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		if (event.getMessage().charAt(0) == '@') {
			String message = event.getMessage();
	      
			if (message.length() == 1) {
				return;
			}
			message = message.replaceFirst("@", "");
			if (player.hasPermission("chat.staff")) {
				final PlayerManager pm = PlayerManager.get(player.getUniqueId());
				for (Player staff : Bukkit.getOnlinePlayers()) {
					if (staff.hasPermission("chat.staff")) {
						event.setCancelled(true);
						staff.sendMessage(ChatColor.GREEN + "(" + ChatColor.RED + "Staff" + ChatColor.GREEN + ") " + pm.getPrefixColors() + player.getName() + ChatColor.GOLD + " » " + message);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void translateColorWhenSignPlaced(SignChangeEvent event) {
		if (!event.getPlayer().isOp()) {
			return;
		}
		for (int i = 0; i < 4; i++) {
			event.setLine(i, ChatColor.translateAlternateColorCodes('&', event.getLine(i)));  
		}
	}
}
