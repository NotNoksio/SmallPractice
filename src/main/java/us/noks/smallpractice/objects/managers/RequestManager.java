package us.noks.smallpractice.objects.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.party.Party;

public class RequestManager {
	public void openLadderSelectionIventory(Player requester, Player requested) {
		if (PlayerManager.get(requester.getUniqueId()).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		Main.getInstance().getInventoryManager().setSelectingDuel(requester.getUniqueId(), requested.getUniqueId());
		requester.openInventory(Main.getInstance().getInventoryManager().getLaddersInventory());
	}
    
    public void sendDuelRequest(Arenas arena, Ladders ladder, Player requester, Player requested) {
    	PlayerManager requesterManager = PlayerManager.get(requester.getUniqueId());
		if (requesterManager.getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		TextComponent line = new TextComponent(requester.getName());
		line.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
	    
		TextComponent lineA = new TextComponent(" has requested you to duel in ");
		lineA.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
		
		TextComponent lineLadder = new TextComponent(ladder.getName());
		lineLadder.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
		
		TextComponent lineOn = new TextComponent(" on ");
		lineOn.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
		
		TextComponent lineArena = new TextComponent(arena.getName() + " arena");
		lineArena.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
		
		TextComponent lineDot = new TextComponent(". ");
		lineDot.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
	    
		TextComponent lineB = new TextComponent("Click here to accept.");
		lineB.setColor(net.md_5.bungee.api.ChatColor.GREEN);
		lineB.setBold(true);
		lineB.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GREEN + "Click this message to accept " + requester.getName()).create()));
		lineB.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + requester.getName()));
		
		TextComponent lineSpace = new TextComponent(" ");
		
		TextComponent lineC = new TextComponent("Click here to deny.");
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
	
	public void acceptDuelRequest(Arenas arena, Ladders ladder, Player requested, Player requester) {
		PlayerManager requesterManager = PlayerManager.get(requester.getUniqueId());
		if (requesterManager.getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		if (!requesterManager.hasRequested(requested.getUniqueId())) {
			requested.sendMessage(ChatColor.RED + "This player doesn't sent you a duel request!");
			return;
		}
		Party requesterParty = Main.getInstance().getPartyManager().getParty(requester.getUniqueId());
        Party requestedParty = Main.getInstance().getPartyManager().getParty(requested.getUniqueId());
        if ((requesterParty != null && requestedParty == null) || (requestedParty != null && requesterParty == null)) {
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
	
	public void sendPartyInvite(Player requester, Player requested) {
		PlayerManager requesterManager = PlayerManager.get(requester.getUniqueId());
		if (requesterManager.getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		TextComponent line = new TextComponent(requester.getName());
		line.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
	    
		TextComponent lineA = new TextComponent(" has invited you into his party. ");
		lineA.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
	    
		TextComponent lineB = new TextComponent("Click here to accept.");
		lineB.setColor(net.md_5.bungee.api.ChatColor.GREEN);
		lineB.setBold(true);
		lineB.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GREEN + "Click this message to accept " + requester.getName()).create()));
		lineB.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + requester.getName()));
		
		TextComponent lineSpace = new TextComponent(" ");
		
		TextComponent lineC = new TextComponent("Click here to deny.");
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
		PlayerManager requesterManager = PlayerManager.get(requester.getUniqueId());
		if (requesterManager.getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		if (!requesterManager.hasInvited(requested.getUniqueId())) {
			requested.sendMessage(ChatColor.RED + "This player doesn't invite you to his party!");
			return;
		}
		Party requesterParty = Main.getInstance().getPartyManager().getParty(requester.getUniqueId());
		requesterManager.getInvites().remove(requested.getUniqueId());
		Main.getInstance().getPartyManager().joinParty(requesterParty.getLeader(), requested.getUniqueId());
		Main.getInstance().getPartyManager().notifyParty(requesterParty, ChatColor.GREEN + requested.getName() + " has joined the party");
        requested.sendMessage(ChatColor.GREEN + "You have joined the party!");
        Main.getInstance().getItemManager().giveSpawnItem(requested);
        Main.getInstance().getPartyManager().updateParty(requesterParty);
	}
	
	public void denyPartyInvite(Player requested, Player requester) {
		if (PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return;
		}
		PlayerManager requesterManager = PlayerManager.get(requester.getUniqueId());
		if (!requesterManager.hasInvited(requested.getUniqueId())) {
			requested.sendMessage(ChatColor.RED + "This player doesn't invite you to his party!");
			return;
		}
		requesterManager.getInvites().remove(requested.getUniqueId());
		requester.sendMessage(ChatColor.YELLOW + requested.getName() + ChatColor.RED + " has denied your party invite!");
		requested.sendMessage(ChatColor.RED + "You deny the party invite from " + ChatColor.YELLOW + requester.getName());
	}
}
