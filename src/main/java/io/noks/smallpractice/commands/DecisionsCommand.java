package io.noks.smallpractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.objects.Request;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class DecisionsCommand implements CommandExecutor {
	private Main main;
	public DecisionsCommand(Main main) {
		this.main = main;
		main.getCommand("accept").setExecutor(this);
		main.getCommand("deny").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (!command.getName().equalsIgnoreCase("accept") && !command.getName().equalsIgnoreCase("deny")) {
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /" + command.getName().toLowerCase() + " <player>");
			return false;
		}
		final Player dueler = this.main.getServer().getPlayer(args[0]);
			
		if (dueler == null) {
			sender.sendMessage(ChatColor.RED + "This player is not online.");
			return false;
		}
		final Player player = (Player) sender;
		
		if (dueler == player) {
			player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
			return false;
		}
		final PlayerManager dm = PlayerManager.get(dueler.getUniqueId());
		if (!dm.hasRequested(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "No request found!");
			return false;
		}
		if (command.getName().equalsIgnoreCase("accept")) {
			final Request request = dm.getRequests().get(player.getUniqueId());
			this.main.getRequestManager().acceptDuelRequest(request.getArena(), request.getLadder(), player, dueler);
			return true;
		}
		this.main.getRequestManager().denyDuelRequest(player, dueler);
		return true;
	}
}
