package us.noks.smallpractice.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import us.noks.smallpractice.utils.InvView;

public class InventoryCommand implements CommandExecutor, Listener {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if ((sender instanceof Player)) {
			Player p = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("inventory")) {
				if (args.length != 1) {
					p.sendMessage(ChatColor.RED + "Usage: /inventory <UUID>");
					return false;
				}
				if (!args[0].matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")) {
					p.sendMessage(ChatColor.RED + "UUID not found!");
					return false;
				}
				/*
				if (args[0] != PlayerManager.get(p).getOldOpponent().getUniqueId().toString()) {
					p.sendMessage(ChatColor.RED + "Inventory not found!");
					return false;
				}*/
				InvView.getInstance().openInv(p, UUID.fromString(args[0]));
			}
		}
		return true;
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onInvsClick(InventoryClickEvent e) {
		if (e.getInventory().getName().endsWith("'s Inventory")) {
			e.setCancelled(true);
			
			/*
			if (e.getCurrentItem() != null && e.getCurrentItem().getType() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null) {
				if (e.getCurrentItem().getItemMeta().getDisplayName().equals(PlayerManager.get(Bukkit.getPlayer(e.getInventory().getTitle().split("'")[0])).getOldOpponent().getName() + "'s Inventory")) {
					InvView.getInstance().openInv((Player) e.getWhoClicked(), PlayerManager.get(Bukkit.getPlayer(e.getInventory().getTitle().split("'")[0])).getOldOpponent().getUniqueId());
				}
			}*/
		}
	}
}