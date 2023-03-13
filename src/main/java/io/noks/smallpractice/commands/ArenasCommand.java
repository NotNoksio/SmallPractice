package io.noks.smallpractice.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.managers.PlayerManager;

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
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		if (pm.getStatus() != PlayerStatus.SPAWN && pm.getStatus() != PlayerStatus.MODERATION && !pm.isAllowedToBuild()) {
			return false;
		}
		player.openInventory(Main.getInstance().getInventoryManager().getAllArenasInInventory());
		return true;
	}
}
