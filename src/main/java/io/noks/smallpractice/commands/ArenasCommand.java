package io.noks.smallpractice.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;

public class ArenasCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (!sender.isOp()) {
			return false;
		}
		if (args.length > 0) {
			return false;
		}
		final Player player = (Player) sender;
		player.openInventory(Main.getInstance().getInventoryManager().getAllArenasInInventory());
		return true;
	}
}
