package io.noks.smallpractice.objects.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.party.Party;
import io.noks.smallpractice.party.PartyState;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class RequestManager {
	public void openLadderSelectionIventory(Player requester, Player requested, boolean partyFight) {
		if (PlayerManager.get(requester.getUniqueId()).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		Main.getInstance().getInventoryManager().setSelectingDuel(requester.getUniqueId(), requested.getUniqueId());
		requester.openInventory(Main.getInstance().getInventoryManager().getLaddersInventory());
	}
    
    public void sendDuelRequest(Arena arena, Ladders ladder, Player requester, Player requested) {
    	final PlayerManager requesterManager = PlayerManager.get(requester.getUniqueId());
		if (requesterManager.getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		final TextComponent line = new TextComponent(requester.getName());
		line.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
	    
		final TextComponent lineA = new TextComponent(" has requested you to duel in ");
		lineA.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
		
		final TextComponent lineLadder = new TextComponent(ladder.getName()); // TODO: if in a party show party size
		lineLadder.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
		
		final TextComponent lineOn = new TextComponent(" on ");
		lineOn.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
		
		final TextComponent lineArena = new TextComponent(arena.getName() + " arena");
		lineArena.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
		
		final TextComponent lineDot = new TextComponent(". ");
		lineDot.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
	    
		final TextComponent lineB = new TextComponent("Click here to accept.");
		lineB.setColor(net.md_5.bungee.api.ChatColor.GREEN);
		lineB.setBold(true);
		lineB.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GREEN + "Click this message to accept " + requester.getName()).create()));
		lineB.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + requester.getName()));
		
		final TextComponent lineSpace = new TextComponent(" ");
		
		final TextComponent lineC = new TextComponent("Click here to deny.");
		lineC.setColor(net.md_5.bungee.api.ChatColor.RED);
		lineC.setBold(true);
		lineC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.RED + "Click this message to deny " + requester.getName()).create()));
		lineC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/deny " + requester.getName()));
	    
		line.addExtra(lineA);
		line.addExtra(lineLadder);
		line.addExtra(lineOn);
		line.addExtra(lineArena);
		line.addExtra(lineDot);
		line.addExtra(lineB);
		line.addExtra(lineSpace);
		line.addExtra(lineC);
	    
		requested.spigot().sendMessage(line);
		requester.sendMessage(ChatColor.DARK_AQUA + "You sent a duel request to " + ChatColor.YELLOW + requested.getName());
		requesterManager.addRequest(requested.getUniqueId(), arena, ladder);
		Main.getInstance().getInventoryManager().removeSelectingDuel(requester.getUniqueId());
	}
	
	public void acceptDuelRequest(Arena arena, Ladders ladder, Player requested, Player requester) {
		final PlayerManager requesterManager = PlayerManager.get(requester.getUniqueId());
		if (requesterManager.getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		if (!requesterManager.hasRequested(requested.getUniqueId())) {
			requested.sendMessage(ChatColor.RED + "This player doesn't sent you a duel request!");
			return;
		}
		final Party requesterParty = Main.getInstance().getPartyManager().getParty(requester.getUniqueId());
        final Party requestedParty = Main.getInstance().getPartyManager().getParty(requested.getUniqueId());
        if (requesterParty != null ^ requestedParty != null) {
            requested.sendMessage(ChatColor.RED + "Either you or this player are in a party!");
            return;
        }
        requesterManager.clearRequest();
		if (requestedParty != null && requesterParty != null) {
			Main.getInstance().getDuelManager().startDuel(arena, ladder, requester.getUniqueId(), requested.getUniqueId(), requesterParty.getAllMembersOnline(), requestedParty.getAllMembersOnline(), false);
			return;
		}
		Main.getInstance().getDuelManager().startDuel(arena, ladder, requester.getUniqueId(), requested.getUniqueId(), false);
	}
	
	public void denyDuelRequest(Player requested, Player requester) {
		if (PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return;
		}
		PlayerManager requesterManager = PlayerManager.get(requester.getUniqueId());
		if (!requesterManager.hasRequested(requested.getUniqueId())) {
			requested.sendMessage(ChatColor.RED + "This player doesn't sent you a duel request!");
			return;
		}
		requesterManager.getRequests().remove(requested.getUniqueId());
		requester.sendMessage(ChatColor.YELLOW + requested.getName() + ChatColor.RED + " has denied your duel request!");
		requested.sendMessage(ChatColor.RED + "You deny the request from " + ChatColor.YELLOW + requester.getName());
	}
	
	public void sendPartyInvite(Party party, Player requested) {
		final PlayerManager requesterManager = PlayerManager.get(party.getLeader());
		final Player requester = requesterManager.getPlayer();
		if (requesterManager.getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		if (party.getPartyState() != PartyState.LOBBY) {
			requester.sendMessage(ChatColor.RED + "You can't do that in your current state!");
			return;
		}
		final TextComponent line = new TextComponent(requester.getName());
		line.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
	    
		final TextComponent lineA = new TextComponent(" has invited you into his party. ");
		lineA.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
	    
		final TextComponent lineB = new TextComponent("Click here to accept.");
		lineB.setColor(net.md_5.bungee.api.ChatColor.GREEN);
		lineB.setBold(true);
		lineB.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GREEN + "Click this message to accept " + requester.getName()).create()));
		lineB.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + requester.getName()));
		
		final TextComponent lineSpace = new TextComponent(" ");
		
		final TextComponent lineC = new TextComponent("Click here to deny.");
		lineC.setColor(net.md_5.bungee.api.ChatColor.RED);
		lineC.setBold(true);
		lineC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.RED + "Click this message to deny " + requester.getName()).create()));
		lineC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party deny " + requester.getName()));
	    
		line.addExtra(lineA);
		line.addExtra(lineB);
		line.addExtra(lineSpace);
		line.addExtra(lineC);
	    
		requested.spigot().sendMessage(line);
		requester.sendMessage(ChatColor.DARK_AQUA + "You sent a party invite to " + ChatColor.YELLOW + requested.getName());
		requesterManager.addInvite(requested.getUniqueId());
	}
	
	public void acceptPartyInvite(Player requested, Player requester) {
		final PlayerManager requesterManager = PlayerManager.get(requester.getUniqueId());
		if (requesterManager.getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		if (!requesterManager.hasInvited(requested.getUniqueId())) {
			requested.sendMessage(ChatColor.RED + "This player have not invited you into his party!");
			return;
		}
		final Party requesterParty = Main.getInstance().getPartyManager().getParty(requester.getUniqueId());
		if (requesterParty.getPartyState() != PartyState.LOBBY) {
			requested.sendMessage(ChatColor.RED + "This party isn't currently accessible!");
			return;
		}
		requesterManager.getInvites().remove(requested.getUniqueId());
		Main.getInstance().getPartyManager().joinParty(requesterParty.getLeader(), requested.getUniqueId());
		requesterParty.notify(ChatColor.GREEN + requested.getName() + " has joined the party");
        requested.sendMessage(ChatColor.GREEN + "You have joined the party!");
        Main.getInstance().getItemManager().giveSpawnItem(requested);
	}
	
	public void denyPartyInvite(Player requested, Player requester) {
		if (PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return;
		}
		final PlayerManager requesterManager = PlayerManager.get(requester.getUniqueId());
		if (!requesterManager.hasInvited(requested.getUniqueId())) {
			requested.sendMessage(ChatColor.RED + "This player doesn't invite you to his party!");
			return;
		}
		requesterManager.getInvites().remove(requested.getUniqueId());
		requester.sendMessage(ChatColor.YELLOW + requested.getName() + ChatColor.RED + " has denied your party invite!");
		requested.sendMessage(ChatColor.RED + "You deny the party invite from " + ChatColor.YELLOW + requester.getName());
	}
}
