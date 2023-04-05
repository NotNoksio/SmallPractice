package io.noks.smallpractice.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.objects.EditedLadderKit;
import io.noks.smallpractice.objects.PlayerSettings;
import io.noks.smallpractice.objects.managers.PlayerManager;
import io.noks.smallpractice.party.Party;

public class ChatListener implements Listener {
	private Main main;
	public ChatListener(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		if (player.hasMetadata("renamekit")) {
			event.setCancelled(true);
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			final EditedLadderKit editedKit = (EditedLadderKit) player.getMetadata("renamekit").get(0).value();
			if (event.getMessage().toLowerCase().equals("cancel")) {
				player.openInventory(this.main.getInventoryManager().getKitEditingLayout(pm, editedKit.getLadder()));
				player.removeMetadata("renamekit", this.main);
				player.sendMessage(ChatColor.RED + "You've cancelled the renaming!");
				return;
			}
			editedKit.rename(event.getMessage());
			player.openInventory(this.main.getInventoryManager().getKitEditingLayout(pm, editedKit.getLadder()));
			player.removeMetadata("renamekit", this.main);
			player.sendMessage(ChatColor.GREEN + "Successfully changed name!");
			return;
		}
		if (event.getMessage().charAt(0) == '@' && player.hasPermission("chat.staff")) {
			String message = event.getMessage();
	      
			if (message.length() == 1) {
				return;
			}
			message = message.replaceFirst("@", "");
			event.setCancelled(true);
			for (Player staff : Bukkit.getOnlinePlayers()) {
				if (staff.hasPermission("chat.staff")) {
					staff.sendMessage(ChatColor.GREEN + "(" + ChatColor.RED + "Staff" + ChatColor.GREEN + ") " + player.getDisplayName() + ChatColor.GOLD + " » " + message);
				}
			}
			return;
		}
		if (event.getMessage().charAt(0) == '!' && this.main.getPartyManager().hasParty(player.getUniqueId())) {
			String message = event.getMessage();
	      
			if (message.length() == 1) {
				return;
			}
			event.setCancelled(true);
			message = message.replaceFirst("!", "");
			final Party party = this.main.getPartyManager().getParty(player.getUniqueId());
			party.notify(ChatColor.YELLOW + "(" + ChatColor.RED + "Party" + ChatColor.YELLOW + ") " + player.getDisplayName() + ChatColor.GOLD + " » " + message);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
	    final String[] args = event.getMessage().split(" ");
	    if(args.length > 1 && (args[0].equalsIgnoreCase("/msg") || args[0].equalsIgnoreCase("/tell") || args[0].equalsIgnoreCase("/w"))) {
	        final Player target = Bukkit.getPlayer(args[1]);
	        if(target == null) {
	            return;
	        }
	        final PlayerSettings settings = PlayerManager.get(target.getUniqueId()).getSettings();
	        if (settings.isPrivateMessageToggled()) {
	        	return;
	        }
	        event.setCancelled(true);
	        event.getPlayer().sendMessage(ChatColor.RED + "This player doesn't allow private message!");
	    }
	}
}
