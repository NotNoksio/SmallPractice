package us.noks.smallpractice.listeners;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import us.noks.smallpractice.objects.managers.PlayerManager;

public class ChatListener implements Listener {

	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		String prefix = "";
		prefix = PlayerManager.get(event.getPlayer()).getColoredPrefix() + "%1$s" + ChatColor.RESET;

		event.setFormat(prefix + ChatColor.WHITE + ": %2$s");
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerGetMentioned(AsyncPlayerChatEvent event) {
		String message = event.getMessage();
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
		Player player = event.getPlayer();
		if (event.getMessage().charAt(0) == '@') {
			String message = event.getMessage();
	      
			message = message.replaceFirst("@", "");
			if (player.hasPermission("chat.staff")) {
				PlayerManager pm = PlayerManager.get(player);
				for (Player staff : Bukkit.getOnlinePlayers()) {
					if (staff.hasPermission("chat.staff")) {
						event.setCancelled(true);
						staff.sendMessage(ChatColor.GREEN + "(" + ChatColor.DARK_AQUA + "Staff" + ChatColor.GREEN + ") " + pm.getPrefixColors() + player.getName() + ChatColor.GOLD + " » " + message);
					}
				}
			}
		}
	}
}
