package us.noks.smallpractice.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class ChatListener implements Listener {

	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if (e.isCancelled()) {
			return;
		}
		String prefix = "";
		prefix = getPrefix(e.getPlayer()) + "%1$s" + ChatColor.RESET;

		e.setFormat(prefix + ChatColor.WHITE + ": %2$s");
	}

	public String getPrefix(Player p) {
		return ChatColor.translateAlternateColorCodes('&', PermissionsEx.getPermissionManager().getUser(p).getPrefix()) + "";
	}
}
