package us.noks.smallpractice.utils;

import org.bukkit.ChatColor;

import us.noks.smallpractice.Main;

public class Messages {
	
	public static String[] WELCOME_MESSAGE = new String[] {
			ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
			ChatColor.YELLOW + "Welcome on the " + ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Halka" + ChatColor.YELLOW + " practice " + Main.getInstance().getDescription().getVersion() + " server",
			"",
			ChatColor.AQUA + "Noksio (Creator) Twitter -> " + ChatColor.DARK_AQUA + "https://twitter.com/NotNoksio",
			ChatColor.BLUE + "Noksio (Creator) Discord -> " + ChatColor.DARK_AQUA + "https://discord.gg/TZhyPnB",
			ChatColor.DARK_PURPLE + "xelo_o (Server Owner) Twitch -> " + ChatColor.DARK_AQUA + "https://www.twitch.tv/xelo_o",
			ChatColor.RED + "-> Keep in mind this is a beta ^^",
			ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"};
	public static String NO_PERMISSION = ChatColor.RED + "No permission.";
	public static String PLAYER_NOT_ONLINE = ChatColor.RED + "This player is not online.";
	
	public static String CONTACT_NOKSIO = ChatColor.RED + "Please contact Noksio!";
}
