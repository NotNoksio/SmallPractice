package us.noks.smallpractice.commands;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// Copying Osu! !roll <integer>
public class RollCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /roll <integer>");
			return false;
		}
		Random roll = new Random();
		int rollNumber = roll.nextInt(100) + 1;
		if (args.length == 1) {
			if (!isInteger(args[0])) {
				sender.sendMessage(ChatColor.RED + "Args 1 need to be an integer!");
				return false;
			}
			rollNumber = roll.nextInt(Integer.valueOf(args[0])) + 1;
		}
		sender.sendMessage(ChatColor.GREEN + sender.getName() + ChatColor.YELLOW + " rolls " + ChatColor.RED + rollNumber + ChatColor.YELLOW + " point(s).");
		return true;
	}
	
	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		} 
		return true;
	}
}