package us.noks.smallpractice.utils;

import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.util.com.google.common.collect.Lists;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.Duel;

public class Messages {
	private static Messages instance = new Messages();
	public static Messages getInstance() {
		return instance;
	}
	
	public final String[] WELCOME_MESSAGE = new String[] {
			ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
			ChatColor.YELLOW + "Welcome on the " + ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Halka" + ChatColor.YELLOW + " practice " + new Main().getDescription().getVersion() + " server",
			"",
			ChatColor.DARK_AQUA + "Noksio (Creator) ↓",
			ChatColor.AQUA + "Twitter -> " + ChatColor.DARK_AQUA + "https://github.com/NotNoksio",
			ChatColor.AQUA + "Github -> " + ChatColor.DARK_AQUA + "https://twitter.com/NotNoksio",
			ChatColor.BLUE + "Discord -> " + ChatColor.DARK_AQUA + "https://discord.gg/BXz5yMF",
			ChatColor.DARK_PURPLE + "xelo_o (Server Owner) ↓",
			ChatColor.DARK_PURPLE + "Twitch -> " + ChatColor.DARK_AQUA + "https://www.twitch.tv/xelo_o",
			"",
			ChatColor.RED + "-> Keep in mind this is a beta ^^",
			ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"};
	public final String NO_PERMISSION = ChatColor.RED + "No permission.";
	public final String PLAYER_NOT_ONLINE = ChatColor.RED + "This player is not online.";
	public final String NOT_YOURSELF = ChatColor.RED + "You can't execute that command on yourself!";
	public final String YOU_ARENT_IN_THE_SPAWN = ChatColor.RED + "You are not in the spawn!";
	public final String TARGET_ARENT_IN_THE_SPAWN = ChatColor.RED + "This player is not in the spawn!";
	public final String TARGET_OR_PLAYER_ARENT_IN_THE_SPAWN = ChatColor.RED + "Either you or this player are not in the spawn!";
	public final String NO_REQUEST_FOUND = ChatColor.RED + "No request found!";
	public final String EMPTY_TEAM = ChatColor.RED + "The duel has been cancelled due to an empty team.";
	
	public final String CONTACT_NOKSIO = ChatColor.RED + "Please contact Noksio!";
	
	public void deathMessage(Duel duel, int winningTeamNumber) {
		List<UUID> winnerTeam = null;
		List<UUID> loserTeam = null;
		switch (winningTeamNumber) {
		case 1:
			winnerTeam = duel.getFirstTeam();
			loserTeam = duel.getSecondTeam();
			break;
		case 2:
			winnerTeam = duel.getSecondTeam();
			loserTeam = duel.getFirstTeam();
			break;
		default:
			break;
		}
		final boolean partyFight = (duel.getFirstTeamPartyLeaderUUID() != null && duel.getSecondTeamPartyLeaderUUID() != null);
		final String winnerMessage = ChatColor.DARK_AQUA + "Winner: " + ChatColor.YELLOW + Bukkit.getPlayer(winnerTeam.get(0)).getName() + (partyFight ? "'s party" : "");
		
	    TextComponent invTxt = new TextComponent();
	    invTxt.setText("Inventories (Click):");
	    invTxt.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
	    
	    List<BaseComponent> inventoriesTextList = Lists.newArrayList();
	    
	    for (UUID wUUID : winnerTeam) {
	    	final Player winners = Bukkit.getPlayer(wUUID);
	    	if (winners == null) continue;
	    	TextComponent wtxt = new TextComponent();
	    	
	    	wtxt.setText(winners.getName());
	    	wtxt.setColor(net.md_5.bungee.api.ChatColor.GREEN);
	    	wtxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to view " + winners.getName() + "'s inventory").create()));
	    	wtxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + winners.getUniqueId()));
		    
		    inventoriesTextList.add(new TextComponent(" "));
		    inventoriesTextList.add(wtxt);
	    }
	    for (UUID lUUID : loserTeam) {
	    	final Player losers = Bukkit.getPlayer(lUUID);
	    	if (losers == null) continue;
	    	TextComponent ltxt = new TextComponent();
	    	
	    	ltxt.setText(losers.getName());
	    	ltxt.setColor(net.md_5.bungee.api.ChatColor.RED);
	    	ltxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to view " + losers.getName() + "'s inventory").create()));
	    	ltxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + losers.getUniqueId()));
		    
		    inventoriesTextList.add(new TextComponent(" "));
		    inventoriesTextList.add(ltxt);
	    }
	    
	    invTxt.setExtra(inventoriesTextList);
	    invTxt.addExtra(net.md_5.bungee.api.ChatColor.DARK_AQUA + ".");
	    
	    StringJoiner spect = new StringJoiner(ChatColor.DARK_AQUA + ", ");
	    if (duel.hasSpectator()) {
	    	for (UUID specs : duel.getAllSpectators()) {
	    		final Player spec = Bukkit.getPlayer(specs);
	    		spect.add(ChatColor.YELLOW + spec.getName());
	    	}
	    }
	    final String spectatorMessage = ChatColor.DARK_AQUA + "Spectator" + (duel.getAllSpectators().size() > 1 ? "s: " : ": ") + spect.toString();
	    
	    List<UUID> duelPlayers = Lists.newArrayList(duel.getFirstAndSecondTeams());
	    duelPlayers.addAll(duel.getAllSpectators());
	    
	    for (UUID dpUUID : duelPlayers) {
	    	final Player duelPlayer = Bukkit.getPlayer(dpUUID);
	    	if (duelPlayer == null) continue;
	    	duelPlayer.sendMessage(winnerMessage);
	    	duelPlayer.spigot().sendMessage(invTxt);
	    	if (duel.hasSpectator()) duelPlayer.sendMessage(spectatorMessage);
	    }
	}
}
